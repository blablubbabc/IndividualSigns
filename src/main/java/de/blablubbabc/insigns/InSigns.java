/**
 * Copyright (c) blablubbabc <http://www.blablubbabc.de>
 * All rights reserved.
 */
package de.blablubbabc.insigns;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import com.comphenix.protocol.wrappers.nbt.NbtWrapper;

public class InSigns extends JavaPlugin implements Listener {

	private ProtocolManager protocolManager;

	@Override
	public void onEnable() {
		protocolManager = ProtocolLibrary.getProtocolManager();

		Bukkit.getPluginManager().registerEvents(this, this);

		// default replacements:

		// [PLAYER] -> player's name
		// permission: insigns.create.player
		new SimpleChanger(this, "[PLAYER]", "insigns.create.player") {

			@Override
			public String getValue(Player player, Location location, String originalLine) {
				return player.getName();
			}
		};

		// [DISPLAY] -> player's display/nick name
		// permission: insigns.create.displayname
		new SimpleChanger(this, "[DISPLAY]", "insigns.create.display") {

			@Override
			public String getValue(Player player, Location location, String originalLine) {
				return player.getDisplayName();
			}
		};

		// register listener for outgoing tile entity data:
		protocolManager.addPacketListener(new PacketAdapter(this, ListenerPriority.NORMAL, PacketType.Play.Server.TILE_ENTITY_DATA) {
			@Override
			public void onPacketSending(PacketEvent event) {
				PacketContainer packet = event.getPacket();
				assert ProtocolUtils.Packet.TileEntityData.isTileEntityDataPacket(packet);
				if (!ProtocolUtils.Packet.TileEntityData.isUpdateSignPacket(packet)) {
					return; // ignore
				}

				Player player = event.getPlayer();
				BlockPosition blockPosition = ProtocolUtils.Packet.TileEntityData.getBlockPosition(packet);
				Location location = new Location(player.getWorld(), blockPosition.getX(), blockPosition.getY(), blockPosition.getZ());
				NbtCompound signData = ProtocolUtils.Packet.TileEntityData.getTileEntityData(packet);
				String[] rawLines = ProtocolUtils.TileEntity.Sign.getText(signData);

				// call the SignSendEvent:
				SignSendEvent signSendEvent = callSignSendEvent(player, location, rawLines);

				if (signSendEvent.isCancelled()) {
					// don't send tile entity update packet:
					event.setCancelled(true);
				} else if (signSendEvent.isModified()) { // only replacing the outgoing packet if it is needed
					String[] newLines = signSendEvent.getLines();

					// prepare new outgoing packet:
					PacketContainer outgoingPacket = packet.shallowClone();
					// create new sign data compound:
					NbtCompound outgoingSignData = NbtFactory.ofCompound(signData.getName());
					// copy tile entity data (shallow copy):
					for (String key : signData.getKeys()) {
						outgoingSignData.put(key, signData.getValue(key));
					}
					// replace lines:
					ProtocolUtils.TileEntity.Sign.setText(outgoingSignData, newLines);
					// use the modified sign data for the outgoing packet:
					ProtocolUtils.Packet.TileEntityData.setTileEntityData(outgoingPacket, outgoingSignData);

					// replace packet for this player:
					event.setPacket(outgoingPacket);
				}
			}
		});

		// register listener for outgoing map chunk packets:
		protocolManager.addPacketListener(new PacketAdapter(this, ListenerPriority.NORMAL, PacketType.Play.Server.MAP_CHUNK) {
			@Override
			public void onPacketSending(PacketEvent event) {
				PacketContainer packet = event.getPacket();
				assert ProtocolUtils.Packet.MapChunk.isMapChunkPacket(packet);
				Player player = event.getPlayer();
				World world = player.getWorld();

				// only replacing the outgoing packet if it is needed:
				PacketContainer outgoingPacket = null;
				List<Object> outgoingTileEntitiesData = null;
				boolean removedSignData = false;

				List<Object> tileEntitiesData = ProtocolUtils.Packet.MapChunk.getTileEntitiesData(packet);
				for (int index = 0, size = tileEntitiesData.size(); index < size; index++) {
					Object nmsTileEntityData = tileEntitiesData.get(index);
					NbtCompound tileEntityData = NbtFactory.fromNMSCompound(nmsTileEntityData);
					if (!ProtocolUtils.TileEntity.Sign.isTileEntitySignData(tileEntityData)) {
						continue; // ignore
					}

					int x = ProtocolUtils.TileEntity.getX(tileEntityData);
					int y = ProtocolUtils.TileEntity.getY(tileEntityData);
					int z = ProtocolUtils.TileEntity.getZ(tileEntityData);
					Location location = new Location(world, x, y, z);
					String[] rawLines = ProtocolUtils.TileEntity.Sign.getText(tileEntityData);

					// call the SignSendEvent:
					SignSendEvent signSendEvent = callSignSendEvent(player, location, rawLines);
					if (signSendEvent.isCancelled() || signSendEvent.isModified()) {
						// prepare new outgoing packet, if we didn't already create one:
						if (outgoingPacket == null) {
							outgoingPacket = packet.shallowClone();
							// copy tile entities data list (shallow copy):
							outgoingTileEntitiesData = new ArrayList<>(tileEntitiesData);
							// use the new tile entities data list for the outgoing packet:
							ProtocolUtils.Packet.MapChunk.setTileEntitiesData(outgoingPacket, outgoingTileEntitiesData);
						}

						if (signSendEvent.isCancelled()) {
							// remove tile entity data for this sign from the outgoing packet:
							// mark the index for later removal by replacing the sign tile entity data with null:
							outgoingTileEntitiesData.set(index, null);
							removedSignData = true;
						} else if (signSendEvent.isModified()) {
							String[] newLines = signSendEvent.getLines();

							// prepare new outgoing packet, if we didn't already create one:
							if (outgoingPacket == null) {
								outgoingPacket = packet.shallowClone();
								// copy tile entities data list (shallow copy):
								outgoingTileEntitiesData = new ArrayList<>(tileEntitiesData);
								// use the new tile entities data list for the outgoing packet:
								ProtocolUtils.Packet.MapChunk.setTileEntitiesData(outgoingPacket, outgoingTileEntitiesData);
							}

							// create new sign data compound:
							NbtCompound outgoingSignData = NbtFactory.ofCompound(tileEntityData.getName());
							// copy tile entity data:
							for (String key : tileEntityData.getKeys()) {
								outgoingSignData.put(key, tileEntityData.getValue(key));
							}
							// replace lines:
							ProtocolUtils.TileEntity.Sign.setText(outgoingSignData, newLines);
							// replace old sign data with the modified sign data in the outgoing packet:
							outgoingTileEntitiesData.set(index, ((NbtWrapper<?>) outgoingSignData).getHandle());
						}
					}
				}

				if (outgoingPacket != null) {
					if (removedSignData) {
						// remove marked (null) tile entity data entries:
						Iterator<Object> iter = outgoingTileEntitiesData.iterator();
						while (iter.hasNext()) {
							if (iter.next() == null) {
								iter.remove();
							}
						}
					}

					// replace packet for this player:
					event.setPacket(outgoingPacket);
				}
			}
		});

		if (this.getConfig().getBoolean("metrics-stats", true)) {
			this.setupMetrics();
		}
	}

	@Override
	public void onDisable() {
		protocolManager = null;
	}

	private void setupMetrics() {
		new Metrics(this);
	}

	public int getPlayerJoinSignUpdateDelay() {
		return this.getConfig().getInt("player-join-sign-update-delay", 2);
	}

	private SignSendEvent callSignSendEvent(Player player, Location location, String[] rawLines) {
		// call the SignSendEvent:
		SignSendEvent signSendEvent = new SignSendEvent(player, location, rawLines);
		Bukkit.getPluginManager().callEvent(signSendEvent);
		return signSendEvent;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	void onPlayerInteract(PlayerInteractEvent event) {
		// ignore off-hand interactions:
		if (event.getHand() != EquipmentSlot.HAND) return;

		if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
			Block block = event.getClickedBlock();
			Material blockType = block.getType();
			if (blockType == Material.WALL_SIGN || blockType == Material.SIGN) {
				// send sign update:
				Player player = event.getPlayer();
				Sign sign = (Sign) block.getState();
				player.sendSignChange(sign.getLocation(), sign.getLines());
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	void onPlayerJoin(PlayerJoinEvent event) {
		int updateDelay = this.getPlayerJoinSignUpdateDelay();
		if (updateDelay <= 0) return;
		final Player player = event.getPlayer();
		final List<Sign> nearbySigns = Utils.getNearbyTileEntities(player.getLocation(), Bukkit.getViewDistance(), Sign.class);
		if (!nearbySigns.isEmpty()) {
			Bukkit.getScheduler().runTaskLater(this, new Runnable() {

				@Override
				public void run() {
					if (!player.isOnline()) return;
					for (Sign sign : nearbySigns) {
						if (Utils.isSign(sign.getBlock().getType())) {
							// still a sign there, send update:
							player.sendSignChange(sign.getLocation(), sign.getLines());
						}
					}
				}
			}, updateDelay);
		}
	}
}
