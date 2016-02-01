package de.blablubbabc.insigns;

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

import com.comphenix.packetwrapper.Packet82UpdateSign;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;

public class InSigns extends JavaPlugin implements Listener {

	@Deprecated
	private final Map<Changer, SimpleChanger> changers = new HashMap<Changer, SimpleChanger>();
	private ProtocolManager protocolManager;

	public void onLoad() {
		protocolManager = ProtocolLibrary.getProtocolManager();
	}

	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);

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

		// PACKETLISTENER
		protocolManager.addPacketListener(new PacketAdapter(this, ListenerPriority.NORMAL, PacketType.Play.Server.UPDATE_SIGN) {
			@Override
			public void onPacketSending(PacketEvent event) {
				Packet82UpdateSign incoming = new Packet82UpdateSign(event.getPacket());

				Player player = event.getPlayer();
				Location location = new Location(player.getWorld(), incoming.getX(), incoming.getY(), incoming.getZ());

				String[] lines = incoming.getLines();
				String[] newLines = { lines[0], lines[1], lines[2], lines[3] };

				// call the SignSendEvent:
				SignSendEvent signSendEvent = new SignSendEvent(player, location, newLines);
				Bukkit.getPluginManager().callEvent(signSendEvent);

				if (signSendEvent.isCancelled()) {
					event.setCancelled(true);
				} else {
					// only replace the outgoing packet if it is needed:
					if (signSendEvent.isModified()) {
						newLines = signSendEvent.getLines();

						// prepare new outgoing packet:
						Packet82UpdateSign outgoing = new Packet82UpdateSign();
						outgoing.setX(incoming.getX());
						outgoing.setY(incoming.getY());
						outgoing.setZ(incoming.getZ());
						outgoing.setLines(newLines);

						event.setPacket(outgoing.getHandle());
					}
				}
			}
		});

		System.out.println(this.toString() + " enabled");
	}

	public void onDisable() {
		System.out.println(this.toString() + " disabled");
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onInteract(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
			Block block = event.getClickedBlock();
			Material blockType = block.getType();
			if (blockType == Material.WALL_SIGN || blockType == Material.SIGN_POST) {
				Sign sign = (Sign) block.getState();
				sendSignChange(event.getPlayer(), sign);
			}
		}
	}

	/**
	 * gets the list of all registered Changers.
	 * 
	 * @return a list of all Changers
	 */
	@Deprecated
	public synchronized List<Changer> getChangerList() {
		return new ArrayList<Changer>(changers.keySet());
	}

	// API
	/**
	 * Sends a UpdateSign-Packet to this, and only this, player. The player must be a valid online player! This is used
	 * to update a sign for a specified user only.
	 * 
	 * @param player
	 *            the player receiving the sign update
	 * @param sign
	 *            the sign to send
	 */
	public void sendSignChange(Player player, Sign sign) {
		if (player == null || !player.isOnline()) return;
		if (sign == null) return;

		String[] lin = sign.getLines();
		PacketContainer result = protocolManager.createPacket(0x82);
		try {
			result.getSpecificModifier(int.class).write(0, sign.getX());
			result.getSpecificModifier(int.class).write(1, sign.getY());
			result.getSpecificModifier(int.class).write(2, sign.getZ());
			result.getStringArrays().write(0, lin);
			protocolManager.sendServerPacket(player, result);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	/**
	 * Removes a Changer for the list of active Changers.
	 * 
	 * @param changer
	 *            the changer that shall be removed
	 */
	@Deprecated
	public synchronized void removeChanger(Changer changer) {
		SimpleChanger changerAdapter = changers.remove(changer);
		HandlerList.unregisterAll(changerAdapter);
	}

	/**
	 * Registers a Changer. By adding a Changer, text on signs that matches the key of this Changer will get replaced
	 * with the individual value specified by the Changers getValue() method. This will remove all active Changers with
	 * an equal key.
	 * 
	 * @param changer
	 *            this Changer is used to specify which text should be replaced with what other text on signs
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
