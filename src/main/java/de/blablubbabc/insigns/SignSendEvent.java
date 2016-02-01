package de.blablubbabc.insigns;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * This event is called whenever the server is about to send a player a sign update packet.
 */
public class SignSendEvent extends Event implements Cancellable {

	private final Player player;
	private final Location location;
	private String[] lines;
	private boolean modified = false;
	private boolean cancelled = false;

	SignSendEvent(Player player, Location location, String[] lines) {
		super(!Bukkit.isPrimaryThread());
		this.player = player;
		this.location = location;
		this.lines = lines;
	}

	/**
	 * Gets the player which receives the sign packet.
	 * 
	 * @return The receiving player.
	 */
	public Player getPlayer() {
		return player;
	}

	/**
	 * Gets the location of the sign data being sent.
	 * 
	 * @return The location of the sent sign data.
	 */
	public Location getLocation() {
		return location;
	}

	/**
	 * Gets the line of text at the specified index.
	 * 
	 * @param index
	 *            Line number to get the text from starting at 0.
	 * @return The sign text on the given line.
	 * @throws IndexOutOfBoundsException
	 *             Thrown when you try to access a line which does not exist.
	 */
	public String getLine(int index) throws IndexOutOfBoundsException {
		return lines[index];
	}

	// non-api method: plugins shouldn't modify the string array directly because we want to keep track if the lines
	// were modified
	String[] getLines() {
		return lines;
	}

	/**
	 * Whether or not this event was already modified.
	 * 
	 * @return True if this event was already modified.
	 */
	public boolean isModified() {
		return modified;
	}

	/**
	 * Sets the line of text at the specified index.
	 * 
	 * @param index
	 *            Line number to set the text at, starting from 0.
	 * @param line
	 *            New text to set at the specified line index.
	 * @throws IndexOutOfBoundsException
	 *             Thrown when you try to access a line which does not exist.
	 */
	public void setLine(int index, String line) throws IndexOutOfBoundsException {
		if (line == null) line = "";
		// ignore if the line wasn't actually changed:
		if (line.equals(lines[index])) return;

		modified = true;
		lines[index] = line;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
