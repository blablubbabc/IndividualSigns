/**
 * Copyright (c) blablubbabc <http://www.blablubbabc.de>
 * All rights reserved.
 */
package de.blablubbabc.insigns;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class InSigns extends JavaPlugin {

	@Override
	public void onEnable() {
		// Register sign packet listeners:
		new SignPacketListeners(this).register();

		// Register player listener:
		Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);

		// Register default sign text replacements:
		DefaultReplacements.register(this);

		// Setup metrics:
		this.setupMetrics();
	}

	private void setupMetrics() {
		if (this.getConfig().getBoolean("metrics-stats", true)) {
			new Metrics(this, 3341);
		}
	}

	@Override
	public void onDisable() {
	}

	public int getPlayerJoinSignUpdateDelay() {
		return this.getConfig().getInt("player-join-sign-update-delay", 2);
	}

	SignSendEvent callSignSendEvent(Player player, Location location, String[] rawLines) {
		SignSendEvent signSendEvent = new SignSendEvent(player, location, rawLines);
		Bukkit.getPluginManager().callEvent(signSendEvent);
		return signSendEvent;
	}
}
