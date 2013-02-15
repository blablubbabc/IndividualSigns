package me.blablubbabc.insigns;

import java.util.ArrayList;
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

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.FieldAccessException;

public class InSigns extends JavaPlugin implements Listener{	

	private List<Changer> changerList;
	private ProtocolManager protocolManager;
	  
	public void onLoad() {
	    protocolManager = ProtocolLibrary.getProtocolManager();
	}

	public void onEnable(){
		getServer().getPluginManager().registerEvents(this, this);

		changerList = new ArrayList<Changer>();
		//Default changers:
		//[PLAYER]

		addChanger(new Changer("[PLAYER]", "player") {

			@Override
			public String getValue(String playerName) {
				return playerName;
			}
		});
		//PACKETLISTENER
		protocolManager.addPacketListener(new PacketAdapter(this,
				ConnectionSide.SERVER_SIDE, ListenerPriority.NORMAL, 0x82) {
			@Override
			public void onPacketSending(PacketEvent event) {
				if (event.getPacketID() == 0x82) {
		            try {
		                event.setPacket(change(event.getPacket(), event.getPlayer().getName()));
		            } catch (FieldAccessException e) {
		                getLogger().log(Level.SEVERE, "Couldn't access field.",
		                        e);
		            }
		        }
			}
		});
		
		System.out.println(this.toString()+" enabled");
	}

	public void onDisable(){
		System.out.println(this.toString()+" disabled");
	}
	
	private PacketContainer change(PacketContainer psign, String playerName) {
		PacketContainer result = protocolManager.createPacket(0x82);
		result.getSpecificModifier(int.class).write(0, psign.getSpecificModifier(int.class).read(0));
		result.getSpecificModifier(int.class).write(1, psign.getSpecificModifier(int.class).read(1));
		result.getSpecificModifier(int.class).write(2, psign.getSpecificModifier(int.class).read(2));
		String[] li = psign.getStringArrays().read(0);
		//Copy because of something important I can't remember..
		String[] lin = {
				li[0],
				li[1],
				li[2],
				li[3],
		};

		ArrayList<Changer> changers = new ArrayList<Changer>(
				getChangerList());
		for (Changer c : changers) {

			String value = c.getValue(playerName);
			String key = c.getKey();

			if (key == null || value == null)
				continue;

			lin[0] = lin[0].replace(key, value);
			lin[1] = lin[1].replace(key, value);
			lin[2] = lin[2].replace(key, value);
			lin[3] = lin[3].replace(key, value);
		}

		// length check:
		for (int i = 0; i < lin.length; i++) {
			if (lin[i].length() > 15)
				lin[i] = lin[i].substring(0, 15);
		}
		result.getStringArrays().write(0, lin);
		return result;
	}

	@EventHandler(priority=EventPriority.HIGH)
	public void onSignCreate(SignChangeEvent event) {
		if(event.isCancelled()) return;
		Player player = event.getPlayer();
		String[] lines = event.getLines();
		for(String l : lines) {
			for(Changer c : getChangerList()) {
				String key = c.getKey();
				String perm = c.getPerm();
				if(l.contains(key)) {
					if(!player.hasPermission("insigns.create."+perm)) {
						event.setCancelled(true);
						player.sendMessage(ChatColor.RED+"No permission to use '"+key+"' on your sign.");
						player.sendMessage(ChatColor.RED+"Missing Permission: '"+perm+"'.");
					}
				}
			}			
		}
	}

	@EventHandler(priority=EventPriority.NORMAL)
	public void onInteract(PlayerInteractEvent event) {
		if(event.getClickedBlock() != null) {
			Block block = event.getClickedBlock();
			BlockState state = block.getState();
			if(state instanceof Sign) {
				Sign sign = (Sign) state;
				sendSignChange(event.getPlayer(), sign);
			}
		}
	}

	public synchronized List<Changer> getChangerList() {
		return changerList;
	}

	//API
	/**
	 * Sends a UpdateSign-Packet to this, and only this, player.
	 * The player must be a valid online player!
	 * This is used to update a sign for a specified user only.
	 * 
	 * @param player Player receiving the sign update.
	 * @param sign Sign to send.
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
		} catch(Exception e) {
			e.printStackTrace();
			return;
		}
	}

	/**
	 * Removes a changer.
	 * @param changer
	 */
	public synchronized void removeChanger(Changer changer) {
		changerList.remove(changer);
	}

	/**
	 * Register a changer.
	 * 
	 * @param changer The changer-object is used to define which text shall be replaced and with what.
	 */
	public synchronized void addChanger(Changer changer) {
		if (changer == null) {
			throw new IllegalArgumentException("Changer cannot be null");
		}
		ArrayList<Changer> changers = new ArrayList<Changer>(changerList);
		for(Changer c : changers) {
			if(c.getKey().equals(changer.getKey())) {
				changerList.remove(c);
			}
		}
		changerList.add(changer);
	}
}
