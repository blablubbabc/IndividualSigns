package de.blablubbabc.insigns;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;

public class InSigns extends JavaPlugin implements Listener {

	@Deprecated
	private final Map<Changer, SimpleChanger> changers = new HashMap<Changer, SimpleChanger>();
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

		// register listener for outgoing sign packets:
		protocolManager.addPacketListener(new PacketAdapter(this, ListenerPriority.NORMAL, PacketType.Play.Server.UPDATE_SIGN) {
			@Override
			public void onPacketSending(PacketEvent event) {
				PacketContainer signUpdatePacket = event.getPacket();
				Player player = event.getPlayer();
				BlockPosition blockPosition = UpdateSignPacketUtility.getLocation(signUpdatePacket);
				Location location = new Location(player.getWorld(), blockPosition.getX(), blockPosition.getY(), blockPosition.getZ());

				// call the SignSendEvent:
				SignSendEvent signSendEvent = new SignSendEvent(player, location, UpdateSignPacketUtility.getLinesAsStrings(signUpdatePacket));
				Bukkit.getPluginManager().callEvent(signSendEvent);

				if (signSendEvent.isCancelled()) {
					event.setCancelled(true);
				} else {
					// only replace the outgoing packet if it is needed:
					if (signSendEvent.isModified()) {
						String[] lines = signSendEvent.getLines();

						// prepare new outgoing packet:
						PacketContainer outgoingPacket = signUpdatePacket.shallowClone();
						UpdateSignPacketUtility.setLinesFromStrings(outgoingPacket, lines);

						event.setPacket(outgoingPacket);
					}
				}
			}
		});

		if (this.getConfig().getBoolean("metrics-stats", true)) {
			try {
				MetricsLite metrics = new MetricsLite(this);
				metrics.start();
			} catch (IOException e) {
				// Failed to submit the stats :-(
			}
		}
	}

	@Override
	public void onDisable() {
		protocolManager = null;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	void onInteract(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
			Block block = event.getClickedBlock();
			Material blockType = block.getType();
			if (blockType == Material.WALL_SIGN || blockType == Material.SIGN_POST) {
				Sign sign = (Sign) block.getState();
				sendSignChange(event.getPlayer(), sign);
			}
		}
	}

	// API

	/**
	 * Gets the list of all registered Changers.
	 * 
	 * @return A list of all active Changers.
	 */
	@Deprecated
	public synchronized List<Changer> getChangerList() {
		return new ArrayList<Changer>(changers.keySet());
	}

	/**
	 * Sends an UpdateSign-Packet to the specified player. This is used to update a sign for the
	 * specified user only.
	 * 
	 * @param player
	 *            The player receiving the sign update.
	 * @param sign
	 *            The sign to send.
	 */
	public static void sendSignChange(Player player, Sign sign) {
		if (player == null || !player.isOnline()) return;
		if (sign == null) return;

		player.sendSignChange(sign.getLocation(), sign.getLines());
	}

	/**
	 * Removes a Changer for the list of active Changers.
	 * 
	 * @param changer
	 *            The changer that shall be removed.
	 */
	@Deprecated
	public synchronized void removeChanger(Changer changer) {
		SimpleChanger changerAdapter = changers.remove(changer);
		HandlerList.unregisterAll(changerAdapter);
	}

	/**
	 * Registers a Changer. By adding a Changer text on signs that matches the key of this Changer
	 * will get replaced with the individual value specified by the Changers getValue() method.
	 * This will replace the active Changer with an equal key if there is such.
	 * 
	 * @param changer
	 *            The Changer handling the replacement of sign text.
	 */
	@Deprecated
	public synchronized void addChanger(final Changer changer) {
		if (changer == null) {
			throw new IllegalArgumentException("Changer cannot be null!");
		}
		// for now to keep compatible to the older api: create adapter sign send listeners:
		SimpleChanger oldChangerAdapter = changers.put(changer, new SimpleChanger(this, changer.getKey(), changer.getPerm()) {

			@Override
			public String getValue(Player player, Location location, String originalLine) {
				return changer.getValue(player, location);
			}
		});
		if (oldChangerAdapter != null) HandlerList.unregisterAll(oldChangerAdapter);
	}
}