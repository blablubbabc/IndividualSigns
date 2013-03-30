package de.blablubbabc.insigns;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.packetwrapper.Packet82UpdateSign;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.FieldAccessException;

public class InSigns extends JavaPlugin implements Listener {

	private List<Changer> changerList;
	private ProtocolManager protocolManager;

	public void onLoad() {
		protocolManager = ProtocolLibrary.getProtocolManager();
	}

	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);

		changerList = new ArrayList<Changer>();
		// Default Changers:
		// [PLAYER] -> insigns.create.player
		addChanger(new Changer("[PLAYER]", "insigns.create.player") {

			@Override
			public String getValue(Player player) {
				return player.getName();
			}
		});

		// PACKETLISTENER
		protocolManager.addPacketListener(new PacketAdapter(this, ConnectionSide.SERVER_SIDE, ListenerPriority.NORMAL, 0x82) {
			@Override
			public void onPacketSending(PacketEvent event) {
				if (event.getPacketID() == 0x82) {
					try {
						event.setPacket(modify(event.getPacket(), event.getPlayer()));
					} catch (FieldAccessException e) {
						getLogger().log(Level.SEVERE, "Couldn't access field.", e);
					}
				}
			}
		});

		System.out.println(this.toString() + " enabled");
	}

	public void onDisable() {
		System.out.println(this.toString() + " disabled");
	}

	private PacketContainer modify(PacketContainer psign, Player player) {
		Packet82UpdateSign incoming = new Packet82UpdateSign(psign);
		Packet82UpdateSign outgoing = new Packet82UpdateSign();
		String[] lines = incoming.getLines();
		String[] newLines = { lines[0], lines[1], lines[2], lines[3] };
		String value = null;
		String key = null;
		for (Changer c : changerList) {
			key = c.getKey();
			if (key == null)
				continue;
			for (int i = 0; i < newLines.length; i++) {
				if (newLines[i].contains(key)) {
					if (value == null) {
						value = c.getValue(player);
						if (value == null)
							break;
					}
					newLines[i] = newLines[i].replace(key, value);
				}
			}
		}
		// checking length:
		for (int i = 0; i < newLines.length; i++) {
			if (newLines[i].length() > 15) {
				if (i < newLines.length - 1 && newLines[i + 1].isEmpty()) {
					newLines[i + 1] = newLines[i].substring(15);
				}
				newLines[i] = newLines[i].substring(0, 15);
			}
		}
		outgoing.setX(incoming.getX());
		outgoing.setY(incoming.getY());
		outgoing.setZ(incoming.getZ());
		outgoing.setLines(newLines);
		return outgoing.getHandle();
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

	@EventHandler(priority = EventPriority.NORMAL)
	public void onInteract(PlayerInteractEvent event) {
		if (event.getClickedBlock() != null) {
			Block block = event.getClickedBlock();
			BlockState state = block.getState();
			if (state instanceof Sign) {
				Sign sign = (Sign) state;
				sendSignChange(event.getPlayer(), sign);
			}
		}
	}

	/**
	 * Returns the list of all registered Changers.
	 * 
	 * @return list of all Changers
	 */
	public synchronized List<Changer> getChangerList() {
		return changerList;
	}

	// API
	/**
	 * Sends a UpdateSign-Packet to this, and only this, player. The player must
	 * be a valid online player! This is used to update a sign for a specified
	 * user only.
	 * 
	 * @param player
	 *            the player receiving the sign update
	 * @param sign
	 *            the sign to send
	 */
	public void sendSignChange(Player player, Sign sign) {
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
	public synchronized void removeChanger(Changer changer) {
		changerList.remove(changer);
	}

	/**
	 * Registers a Changer. By adding a Changer, text on signs that matches the
	 * key of this Changer will get replaced with the individual value specified
	 * by the Changers getValue() method. This will remove all active Changers
	 * with an equal key.
	 * 
	 * @param changer
	 *            this Changer is used to specify which text should be replaced
	 *            with what other text on signs
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
