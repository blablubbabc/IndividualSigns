/**
 * Copyright (c) blablubbabc <http://www.blablubbabc.de>
 * All rights reserved.
 */
package de.blablubbabc.insigns;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * This event is called whenever the server is about to update a sign's contents for a specific player.
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
	 * Gets the player which receives the sign content update.
	 * 
	 * @return the receiving player
	 */
	public Player getPlayer() {
		return player;
	}

	/**
	 * Gets the location of the sign for which data is being sent.
	 * 
	 * @return the location of the sign
	 */
	public Location getLocation() {
		return location.clone();
	}

	/**
	 * Gets the line of text at the specified index.
	 * 
	 * <p>
	 * This will be the raw line content, as it is contained in the corresponding packet.
	 * </p>
	 * 
	 * @param index
	 *            the line number to get the text from, starting at 0
	 * @return the sign text on the given line
	 * @throws IndexOutOfBoundsException
	 *             when trying to access a line which does not exist
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
	 * Whether or not this event was modified.
	 * 
	 * @return <code>true</code> if this event was modified, <code>false</code> otherwise
	 */
	public boolean isModified() {
		return modified;
	}

	/**
	 * Sets the line of text at the specified index.
	 * 
	 * <p>
	 * This expects the raw line content, as it is contained in the corresponding packet.
	 * </p>
	 * 
	 * @param index
	 *            the line number to set the text at, starting at 0
	 * @param line
	 *            the new text to set at the specified line index
	 * @throws IndexOutOfBoundsException
	 *             when trying to access a line which does not exist
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

	/**
	 * Canceling this event will prevent the sign content from updating for the affected player.
	 */
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