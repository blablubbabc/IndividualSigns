/**
 * Copyright (c) blablubbabc <http://www.blablubbabc.de>
 * All rights reserved.
 */
package de.blablubbabc.insigns;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.plugin.Plugin;

/**
 * Uses the {@link SignSendEvent} to replace a given fixed {@code key} within the sign text with a certain, potentially
 * dynamic {@link #getValue(Player, Location, String) value} for the player who receives the sign contents.
 */
public class SimpleChanger implements Listener {

	private final String key;
	private final String permissionsNode;

	/**
	 * Creates a new {@link SimpleChanger}.
	 * <p>
	 * This automatically registers an event handler for the {@link SignSendEvent} which implements the key-value sign
	 * text replacement.
	 * 
	 * @param plugin
	 *            the plugin, not <code>null</code>, needs to be enabled
	 * @param key
	 *            the key to replace within the sign text, not <code>null</code> or empty
	 * @param permissionsNode
	 *            the permissions node that is required to create signs that contain the key, not <code>null</code> or
	 *            empty
	 */
	public SimpleChanger(Plugin plugin, String key, String permissionsNode) {
		Validate.notNull(plugin, "plugin");
		Validate.isTrue(plugin.isEnabled(), "plugin is not enabled");
		Validate.notNull(key, "key");
		Validate.isTrue(!key.isEmpty(), "key is empty");
		Validate.notNull(permissionsNode, "permissionsNode");
		Validate.isTrue(!permissionsNode.isEmpty(), "permissionsNode is empty");

		this.key = key;
		this.permissionsNode = permissionsNode;

		Bukkit.getPluginManager().registerEvents(this, plugin);
	}

	/**
	 * Gets the value that replaces the {@code key} within the text of a sign that is being sent to the specified
	 * player.
	 * 
	 * @param player
	 *            the player receiving the new sign text
	 * @param signLocation
	 *            the location of the sign
	 * @param originalLine
	 *            this parameter is no longer used and simply matches the key
	 * @return the value, not <code>null</code>
	 * @deprecated Use {@link #getValue(Player, Location)} instead.
	 */
	@Deprecated // Since 2.8.0
	public String getValue(Player player, Location signLocation, String originalLine) {
		return this.getValue(player, signLocation); // Delegate to the new method
	}

	/**
	 * Gets the value that replaces the {@code key} within the text of a sign that is being sent to the specified
	 * player.
	 * <p>
	 * This is called at most once per sign whose text is being sent to the player. The returned value is then reused
	 * for all occurrences of the key within the text of this sign.
	 * 
	 * @param player
	 *            the player receiving the new sign text
	 * @param signLocation
	 *            the location of the sign
	 * @return the value, not <code>null</code>
	 */
	public String getValue(Player player, Location signLocation) {
		return key;
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onSignSend(SignSendEvent event) {
		String value = null;
		for (int i = 0; i < 4; i++) {
			String line = event.getLine(i);
			if (line.contains(key)) {
				if (value == null) {
					value = this.getValue(event.getPlayer(), event.getLocation(), key);
					// Ensure that the value is no longer null so that we request it at most once per sent sign:
					if (value == null) {
						value = "null";
					}
				}
				event.setLine(i, line.replace(key, value));
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onSignCreate(SignChangeEvent event) {
		Player player = event.getPlayer();
		String[] lines = event.getLines();
		for (String line : lines) {
			if (line.contains(key)) {
				if (!player.hasPermission(permissionsNode)) {
					event.setCancelled(true);
					player.sendMessage(ChatColor.RED + "Missing permission '" + permissionsNode + "' to use '" + key + "' on your sign.");
					return;
				}
			}
		}
	}
}
