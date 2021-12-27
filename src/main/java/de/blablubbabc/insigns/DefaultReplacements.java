/**
 * Copyright (c) blablubbabc <http://www.blablubbabc.de>
 * All rights reserved.
 */
package de.blablubbabc.insigns;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.google.common.base.Preconditions;

/**
 * The default sign text replacements.
 */
final class DefaultReplacements {

	private DefaultReplacements() {
	}

	static void register(Plugin plugin) {
		Preconditions.checkNotNull(plugin, "plugin is null");

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
