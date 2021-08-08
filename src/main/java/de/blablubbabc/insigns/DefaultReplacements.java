/**
 * Copyright (c) blablubbabc <http://www.blablubbabc.de>
 * All rights reserved.
 */
package de.blablubbabc.insigns;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * The default sign text replacements.
 */
class DefaultReplacements {

	private DefaultReplacements() {
	}

	static void register(Plugin plugin) {
		Validate.notNull(plugin, "plugin");

		// [PLAYER] -> Player's name
		// Permission: 'insigns.create.player'
		new SimpleChanger(plugin, "[PLAYER]", "insigns.create.player") {
			@Override
			public String getValue(Player player, Location location) {
				return player.getName();
			}
		};

		// [DISPLAY] -> Player's display name
		// Permission: 'insigns.create.displayname'
		new SimpleChanger(plugin, "[DISPLAY]", "insigns.create.display") {
			@Override
			public String getValue(Player player, Location location) {
				return player.getDisplayName();
			}
		};
	}
}
