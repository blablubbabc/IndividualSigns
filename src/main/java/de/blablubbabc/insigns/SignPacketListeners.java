/**
 * Copyright (c) blablubbabc <http://www.blablubbabc.de>
 * All rights reserved.
 */
package de.blablubbabc.insigns;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.InternalStructure;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import com.google.common.base.Preconditions;

/**
 * Packet listeners that are responsible for calling the {@link SignSendEvent} and replacing the outgoing sign contents
 * if needed.
 */
class SignPacketListeners {

	private final InSigns plugin;

	SignPacketListeners(InSigns plugin) {
		Preconditions.checkNotNull(plugin, "plugin is null");
		this.plugin = plugin;
	}

	void register() {
		ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
		// Register a packet listener for outgoing tile entity data:
		protocolManager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.NORMAL, PacketType.Play.Server.TILE_ENTITY_DATA) {
			@Override
			public void onPacketSending(PacketEvent event) {
				onTileEntityDataSending(event);
			}
		});

		// Register a packet listener for outgoing chunk data:
		protocolManager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.NORMAL, PacketType.Play.Server.MAP_CHUNK) {
			@Override
			public void onPacketSending(PacketEvent event) {
				onChunkSending(event);
			}
		});
	}

	private void onTileEntityDataSending(PacketEvent event) {
		PacketContainer packet = event.getPacket();
		assert ProtocolUtils.Packet.TileEntityData.isTileEntityDataPacket(packet);
		// TODO Identify the tile entity type here based on the BlockEntityType stored by the packet.
		/*if (!ProtocolUtils.Packet.TileEntityData.isUpdateSignPacket(packet)) {
			return; // Ignore
		}*/

		// Call the SignSendEvent:
		Player player = event.getPlayer();
		BlockPosition blockPosition = ProtocolUtils.Packet.TileEntityData.getBlockPosition(packet);
		Location location = new Location(player.getWorld(), blockPosition.getX(), blockPosition.getY(), blockPosition.getZ());
		NbtCompound signData = ProtocolUtils.Packet.TileEntityData.getTileEntityData(packet);
		// TODO Identify the tile entity type based on the BlockEntityType stored by the packet.
		if (!ProtocolUtils.TileEntity.Sign.isTileEntitySignData(signData)) {
			return; // Ignore
		}
		String[] rawLines = ProtocolUtils.TileEntity.Sign.getText(signData);

		SignSendEvent signSendEvent = plugin.callSignSendEvent(player, location, rawLines);

		if (signSendEvent.isCancelled()) {
			// Cancelled: Do not send the tile entity update packet.
			event.setCancelled(true);
		} else if (signSendEvent.isModified()) { // Only replace the outgoing packet if needed
			// Prepare the new outgoing packet:
			PacketContainer outgoingPacket = packet.shallowClone();

			// Prepare the new sign data:
			String[] newLines = signSendEvent.getLines();
			NbtCompound outgoingSignData = replaceSignData(signData, newLines);

			// Use the new modified sign data for the new outgoing packet:
			ProtocolUtils.Packet.TileEntityData.setTileEntityData(outgoingPacket, outgoingSignData);

			// Replace the outgoing packet for the player:
			event.setPacket(outgoingPacket);
		}
	}

	private void onChunkSending(PacketEvent event) {
		PacketContainer packet = event.getPacket();
		assert ProtocolUtils.Packet.MapChunk.isMapChunkPacket(packet);
		Player player = event.getPlayer();
		World world = player.getWorld();

		int chunkBlockX = ProtocolUtils.Packet.MapChunk.getChunkX(packet) * 16;
		int chunkBlockZ = ProtocolUtils.Packet.MapChunk.getChunkZ(packet) * 16;

		// Only replace the outgoing packet if needed:
		PacketContainer outgoingPacket = null;
		List<InternalStructure> outgoingTileEntitiesInfo = null;
		boolean removedSignData = false;

		List<InternalStructure> tileEntitiesInfo = ProtocolUtils.Packet.MapChunk.getTileEntitiesInfo(packet);
		for (int index = 0, size = tileEntitiesInfo.size(); index < size; index++) {
			InternalStructure tileEntityInfo = tileEntitiesInfo.get(index);
			NbtCompound tileEntityData = ProtocolUtils.Packet.MapChunk.TileEntityInfo.getNbt(tileEntityInfo);
			if (tileEntityData == null) {
				continue;
			}

			// TODO Identify the tile entity type based on the BlockEntityType stored by the tileEntityInfo.
			if (!ProtocolUtils.TileEntity.Sign.isTileEntitySignData(tileEntityData)) {
				continue; // Ignore
			}

			// Call the SignSendEvent:
			int x = ProtocolUtils.Packet.MapChunk.TileEntityInfo.getLocalX(tileEntityInfo) + chunkBlockX;
			int z = ProtocolUtils.Packet.MapChunk.TileEntityInfo.getLocalZ(tileEntityInfo) + chunkBlockZ;
			int y = ProtocolUtils.Packet.MapChunk.TileEntityInfo.getY(tileEntityInfo);
			Location location = new Location(world, x, y, z);
			String[] rawLines = ProtocolUtils.TileEntity.Sign.getText(tileEntityData);

			SignSendEvent signSendEvent = plugin.callSignSendEvent(player, location, rawLines);

			if (signSendEvent.isCancelled() || signSendEvent.isModified()) {
				// Prepare a new outgoing packet, if we didn't already create one:
				if (outgoingPacket == null) {
					outgoingPacket = packet.shallowClone();
					// Copy the previous list of tile entity info (shallow copy):
					outgoingTileEntitiesInfo = new ArrayList<>(tileEntitiesInfo);
					// Use the new tile entities info list for the new outgoing packet:
					ProtocolUtils.Packet.MapChunk.setTileEntitiesInfo(outgoingPacket, outgoingTileEntitiesInfo);
				}

				if (signSendEvent.isCancelled()) {
					// Remove the tile entity info for this sign from the outgoing packet:
					// Mark the index for later removal by replacing the sign's tile entity info with null.
					outgoingTileEntitiesInfo.set(index, null);
					removedSignData = true;
				} else if (signSendEvent.isModified()) {
					// Prepare the new sign data:
					String[] newLines = signSendEvent.getLines();
					NbtCompound outgoingSignData = replaceSignData(tileEntityData, newLines);

					// Replace the old sign info with the new modified sign info in the new outgoing packet:
					InternalStructure newTileEntityInfo = ProtocolUtils.Packet.MapChunk.TileEntityInfo.cloneWithNewNbt(tileEntityInfo, outgoingSignData);
					outgoingTileEntitiesInfo.set(index, newTileEntityInfo);
				}
			}
		}

		if (outgoingPacket != null) {
			if (removedSignData) {
				// Remove marked (null) tile entity info entries:
				Iterator<InternalStructure> iter = outgoingTileEntitiesInfo.iterator();
				while (iter.hasNext()) {
					if (iter.next() == null) {
						iter.remove();
					}
				}
			}

			// Replace the outgoing packet for the player:
			event.setPacket(outgoingPacket);
		}
	}

	/**
	 * Copies the given sign data and applies the given replacement text.
	 * 
	 * @param previousSignData
	 *            the previous sign data, not <code>null</code>
	 * @param newSignText
	 *            the new sign text lines
	 * @return the new sign data
	 */
	private NbtCompound replaceSignData(NbtCompound previousSignData, String[] newSignText) {
		NbtCompound newSignData = NbtFactory.ofCompound(previousSignData.getName());

		// Copy the previous tile entity data (shallow copy):
		for (String key : previousSignData.getKeys()) {
			newSignData.put(key, previousSignData.getValue(key));
		}

		// Replace the sign text:
		ProtocolUtils.TileEntity.Sign.setText(newSignData, newSignText);

		return newSignData;
	}
}
