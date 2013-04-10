package unused;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SignSendEvent extends Event {
	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
        return handlers;
    }
	
	private final Player player;
	private final Location location;
	private final String[] lines;
	
	public SignSendEvent(Player player, Location location, String[] lines) {
		this.player = player;
		this.location = location;
		this.lines = lines;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public Location getLocation() {
		return location;
	}

	public String[] getLines() {
		return lines;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
}
