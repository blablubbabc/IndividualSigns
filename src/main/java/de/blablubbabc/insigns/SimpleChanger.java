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
	 * <p>
	 * This is called for every line of sign text that contains the key.
	 * 
	 * @param player
	 *            the player receiving the new sign text
	 * @param location
	 *            the location of the sign
	 * @param originalLine
	 *            the original line of sign text that is being modified
	 * @return the value
	 */
	public String getValue(Player player, Location location, String originalLine) {
		return key;
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onSignSend(SignSendEvent event) {
		for (int i = 0; i < 4; i++) {
			String line = event.getLine(i);
			if (line.contains(key)) {
				event.setLine(i, line.replace(key, this.getValue(event.getPlayer(), event.getLocation(), line)));
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
