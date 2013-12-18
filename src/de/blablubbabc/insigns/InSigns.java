package de.blablubbabc.insigns;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.FieldAccessException;

public class InSigns extends JavaPlugin implements Listener {

	private List<Changer> changerList;
	private ProtocolManager protocolManager;

	@Override
	public void onLoad() {
		protocolManager = ProtocolLibrary.getProtocolManager();
	}

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);

		changerList = new ArrayList<Changer>();
		// Default Changers:
		// [PLAYER] -> insigns.create.player
		addChanger(new Changer("[PLAYER]", "insigns.create.player") {

			@Override
			public String getValue(final Player player, final Location location) {
				return player.getName();
			}
		});

		// PACKETLISTENER
		protocolManager.addPacketListener(new PacketAdapter(this, ListenerPriority.NORMAL, PacketType.Play.Server.UPDATE_SIGN) {
			@Override
			public void onPacketSending(PacketEvent event) {
				try {
					event.setPacket(modify(event.getPacket(), event.getPlayer()));
				} catch (FieldAccessException e) {
					getLogger().log(Level.SEVERE, "Couldn't access field.", e);
				}
			}
		});

		System.out.println(this.toString() + " enabled");
	}

	@Override
	public void onDisable() {
		System.out.println(this.toString() + " disabled");
	}

	private PacketContainer modify(PacketContainer psign, Player player) {
		PacketUpdateSignWrapper incoming = new PacketUpdateSignWrapper(psign);

		Location location = new Location(player.getWorld(), incoming.getX(), incoming.getY(), incoming.getZ());

		String[] lines = incoming.getLines();

		/*
		 * Maybe use the bukkit event system at a later state: SignSendEvent signEvent = new
		 * SignSendEvent(player, location, newLines);
		 * getServer().getPluginManager().callEvent(signEvent);
		 */

		// the packet only needs to be cloned, if it has to be modified
		boolean modified = false;

		String value = null;
		String key = null;
		for (Changer c : changerList) {
			value = null;
			key = c.getKey();
			if (key == null) continue;
			for (int i = 0; i < lines.length; i++) {
				if (lines[i].contains(key)) {
					modified = true;
					if (value == null) {
						value = c.getValue(player, location);
						if (value == null) break;
					}
					lines[i] = lines[i].replace(key, value);
				}
			}
		}

		if (modified) {
			// checking length:
			for (int i = 0; i < lines.length; i++) {
				if (lines[i].length() > 15) {
					if (i < lines.length - 1 && lines[i + 1].isEmpty()) {
						lines[i + 1] = lines[i].substring(15);
					}
					lines[i] = lines[i].substring(0, 15);
				}
			}

			// prepare new packet:
			PacketUpdateSignWrapper outgoing = new PacketUpdateSignWrapper(psign.shallowClone());
			String[] newLines = { lines[0], lines[1], lines[2], lines[3] };
			outgoing.setLines(newLines);

			return outgoing.getPacket();
		} else {
			return psign;
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onSignCreate(SignChangeEvent event) {
		Player player = event.getPlayer();
		String[] lines = event.getLines();
		String key;
		String perm;
		for (String l : lines) {
			for (Changer changer : changerList) {
				key = changer.getKey();
				perm = changer.getPerm();
				if (l.contains(key)) {
					if (!player.hasPermission(perm)) {
						event.setCancelled(true);
						player.sendMessage(ChatColor.RED + "No permission to use '" + key + "' on your sign.");
						player.sendMessage(ChatColor.RED + "Missing Permission: '" + perm + "'");
					}
				}
			}
		}
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
	 * Gets the list of all registered Changers.
	 * 
	 * @return a list of all Changers
	 */
	public synchronized List<Changer> getChangerList() {
		return changerList;
	}

	// API
	/**
	 * Sends a UpdateSign-Packet to this, and only this, player. The player must be a valid online
	 * player! This is used to update a sign for a specified user only.
	 * 
	 * @param player
	 *            the player receiving the sign update
	 * @param sign
	 *            the sign to send
	 */
	public void sendSignChange(Player player, Sign sign) {
		String[] lines = sign.getLines();
		PacketContainer result = protocolManager.createPacket(PacketType.Play.Server.UPDATE_SIGN);
		try {
			result.getSpecificModifier(int.class).write(0, sign.getX());
			result.getSpecificModifier(int.class).write(1, sign.getY());
			result.getSpecificModifier(int.class).write(2, sign.getZ());
			result.getStringArrays().write(0, lines);
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
	public synchronized void removeChanger(Changer changer) {
		changerList.remove(changer);
	}

	/**
	 * Registers a Changer. By adding a Changer, text on signs that matches the key of this Changer
	 * will get replaced with the individual value specified by the Changers getValue() method. This
	 * will remove all active Changers with an equal key.
	 * 
	 * @param changer
	 *            this Changer is used to specify which text should be replaced with what other text
	 *            on signs
	 */
	public synchronized void addChanger(Changer changer) {
		if (changer == null) {
			throw new IllegalArgumentException("Changer cannot be null!");
		}
		String key = changer.getKey();
		Iterator<Changer> iter = changerList.iterator();
		while (iter.hasNext()) {
			if (iter.next().getKey().equals(key)) {
				iter.remove();
			}
		}
		changerList.add(changer);
	}
}
