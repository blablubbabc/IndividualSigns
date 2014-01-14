package de.blablubbabc.insigns;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * This event is called whenever the server is about to send a player a sign update packet.
 */
public class SignSendEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}

	private final Player player;
	private final Location location;
	private String[] lines;
	private boolean modified = false;
	private boolean cancelled = false;

	SignSendEvent(Player player, Location location, String[] lines) {
		this.player = player;
		this.location = location;
		this.lines = lines;
	}

	/**
	 * Gets the player which receives the sign packet.
	 * 
	 * @return the receiving player
	 */
	public Player getPlayer() {
		return this.player;
	}

	/**
	 * Gets the location of the sign data being sent.
	 * 
	 * @return the location of the sent sign data
	 */
	public Location getLocation() {
		return this.location;
	}

	/**
	 * Gets the line of text at the specified index.
	 * 
	 * @param index
	 *            line number to get the text from, starting at 0
	 * @return the sign text on the given line
	 * @throws IndexOutOfBoundsException
	 *             thrown when you try to access a line which does not exist
	 */
	public String getLine(int index) throws IndexOutOfBoundsException {
		return this.lines[index];
	}

	// non-api method: plugins shouldn't modify the string array directly, because it originally is
	// shared among the sign update packets for multiple players and only gets copied when the first
	// plugin modifies it via the setLine() method
	String[] getLines() {
		return this.lines;
	}

	/**
	 * Whether or not this event was already modified.
	 * 
	 * @return true, if this event was already modified
	 */
	public boolean isModified() {
		return this.modified;
	}

	/**
	 * Sets the line of text at the specified index. Lines longer than the allowed 15 characters
	 * will be either cut or continued in the next lines (if those are empty) AFTER the event is
	 * over.
	 * 
	 * @param index
	 *            line number to set the text at, starting from 0
	 * @param line
	 *            new text to set at the specified index
	 * @throws IndexOutOfBoundsException
	 *             thrown when you try to access a line which does not exist
	 */
	public void setLine(int index, String line) throws IndexOutOfBoundsException {
		if (line == null) line = "";
		// only copy the string array if really needed:
		if (!this.modified) {
			this.modified = true;
			this.lines = new String[] { this.lines[0], this.lines[1], this.lines[2], this.lines[3] };
		}
		this.lines[index] = line;
	}

	@Override
	public boolean isCancelled() {
		return this.cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
}
