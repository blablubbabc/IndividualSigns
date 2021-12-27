/**
 * Copyright (c) blablubbabc <http://www.blablubbabc.de>
 * All rights reserved.
 */
package de.blablubbabc.insigns;

import java.util.List;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Sign;
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

		// Update the nearby signs of all already online players:
		this.updateNearbySignsOfOnlinePlayersDelayed();
	}

	private void setupMetrics() {
		if (this.getConfig().getBoolean("metrics-stats", true)) {
			new Metrics(this, 3341);
		}
	}

	private void updateNearbySignsOfOnlinePlayersDelayed() {
		int delayTicks = this.getPlayerJoinSignUpdateDelay();
		if (delayTicks <= 0) return; // Disabled

		Bukkit.getScheduler().runTaskLater(this, () -> {
			for (Player player : Bukkit.getOnlinePlayers()) {
				this.updateNearbySigns(player);
			}
		}, delayTicks);
	}

	@Override
	public void onDisable() {
	}

	public int getPlayerJoinSignUpdateDelay() {
		return this.getConfig().getInt("player-join-sign-update-delay", 2);
	}

	void updateNearbySigns(Player player) {
		assert player != null && player.isOnline();
		List<Sign> nearbySigns = Utils.getNearbyTileEntities(player.getLocation(), Bukkit.getViewDistance(), Sign.class);
		for (Sign sign : nearbySigns) {
			Utils.sendSignUpdate(player, sign);
		}
	}

	void updateNearbySignsDelayed(Player player, int delayTicks) {
		if (delayTicks <= 0) return; // Disabled
		Bukkit.getScheduler().runTaskLater(this, () -> {
			if (!player.isOnline()) return;
			updateNearbySigns(player);
		}, delayTicks);
	}

	SignSendEvent callSignSendEvent(Player player, Location location, String[] rawLines) {
		SignSendEvent signSendEvent = new SignSendEvent(player, location, rawLines);
		Bukkit.getPluginManager().callEvent(signSendEvent);
		return signSendEvent;
	}
}
