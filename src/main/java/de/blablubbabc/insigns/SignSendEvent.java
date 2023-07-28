/**
 * Copyright (c) blablubbabc <http://www.blablubbabc.de>
 * All rights reserved.
 */
package de.blablubbabc.insigns;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * This event is called whenever the server is about to update the contents of a sign for a specific player.
 */
public class SignSendEvent extends Event implements Cancellable {

	private final Player player;
	private final Location location;
	private String[] linesFront;
	private String[] linesBack;
	private boolean modified = false;
	private boolean cancelled = false;

	SignSendEvent(Player player, Location location, String[] linesFront, String[] linesBack) {
		super(!Bukkit.isPrimaryThread());
		this.player = player;
		this.location = location;
		this.linesFront = linesFront;
		this.linesBack = linesBack;
	}

	/**
	 * Gets the player who receives the updated sign contents.
	 * 
	 * @return the receiving player
	 */
	public Player getPlayer() {
		return player;
	}

	/**
	 * Gets the location of the sign for which the content is being sent.
	 * 
	 * @return the location of the sign
	 */
	public Location getLocation() {
		return location.clone();
	}

	/**
	 * Gets the sign text at the line with the specified index.
	 * <p>
	 * This is the raw line content as it is contained in the corresponding sign update packet.
	 * 
	 * @param index
	 *            the line number to get the text from, starting at 0
	 * @return the sign text for the specified line
	 * @throws IndexOutOfBoundsException
	 *             when trying to access a line that does not exist
	 */
	@Deprecated(forRemoval = true)
	public String getLine(int index) throws IndexOutOfBoundsException {
		return getLine(Side.FRONT, index);
	}


	/**
	 * Gets the sign text at the line with the specified index.
	 * <p>
	 * This is the raw line content as it is contained in the corresponding sign update packet.
	 * 
	 * @param side
	 *            the side to get the text from
	 * @param index
	 *            the line number to get the text from, starting at 0
	 * @return the sign text for the specified line
	 * @throws IndexOutOfBoundsException
	 *             when trying to access a line that does not exist
	 */
	public String getLine(Side side, int index) throws IndexOutOfBoundsException {
		return getLines(side)[index];
	}

	// Non-API method: Plugins should not modify the String array directly because we want to keep track if the lines
	// were modified.
	String[] getLines(Side side) {
		return switch (side) {
			case FRONT -> linesFront;
			case BACK -> linesBack;
			default -> throw new IllegalArgumentException("side");
		};
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
	 * Sets the sign text at the line with the specified index.
	 * <p>
	 * This expects the raw line content as it would be contained in the corresponding packet.
	 * 
	 * @param index
	 *            the line number to set the text at, starting at 0
	 * @param line
	 *            the new text for the specified line
	 * @throws IndexOutOfBoundsException
	 *             when trying to access a line that does not exist
	 */
	@Deprecated(forRemoval = true)
	public void setLine(int index, String line) throws IndexOutOfBoundsException {
		setLine(Side.FRONT, index, line);
	}

	/**
	 * Sets the sign text at the line with the specified index.
	 * <p>
	 * This expects the raw line content as it would be contained in the corresponding packet.
	 * 
	 * @param side
	 *            the side to set the text at
	 * @param index
	 *            the line number to set the text at, starting at 0
	 * @param line
	 *            the new text for the specified line
	 * @throws IndexOutOfBoundsException
	 *             when trying to access a line that does not exist
	 */
	public void setLine(Side side, int index, String line) throws IndexOutOfBoundsException {
		if (line == null) line = "";
		String[] lines = getLines(side);
		// Ignore if the line did not actually change:
		if (line.equals(lines[index])) return;

		modified = true;
		lines[index] = line;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	/**
	 * Canceling this event will prevent the sign contents from being updated for the affected player.
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
