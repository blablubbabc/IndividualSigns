/**
 * Copyright (c) blablubbabc <http://www.blablubbabc.de>
 * All rights reserved.
 */
package de.blablubbabc.insigns;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class InSigns extends JavaPlugin {

	@Override
	public void onEnable() {
		// Load all plugin classes right away:
		// This avoids class loading issues when the jar file is replaced while the plugin is still running.
		this.loadAllPluginClasses();

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

	private void loadAllPluginClasses() {
		File pluginJarFile = this.getFile();
		long startNanos = System.nanoTime();
		boolean success = ClassUtils.loadAllClassesFromJar(pluginJarFile, className -> true, this.getLogger());
		if (success) {
			long durationMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
			this.getLogger().info("Loaded all plugin classes (" + durationMillis + " ms).");
		}
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

	// Note: On newer versions of Minecraft, the chunk packets can often be sent to players with a delay of several
	// ticks, so there is not much benefit anymore in sending these additional (and now earlier) sign updates. However,
	// it is unclear whether the order and delay in which chunk packets are sent can non-deterministically fluctuate.
	// Also, there is the possibility that these aspects of chunk sending will behave differently in future versions of
	// Minecraft, or on different server derivatives. We therefore keep these additional sign updates for now.
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

	SignSendEvent callSignSendEvent(Player player, Location location, String[] rawLinesFront, String[] rawLinesBack) {
		SignSendEvent signSendEvent = new SignSendEvent(player, location, rawLinesFront, rawLinesBack);
		Bukkit.getPluginManager().callEvent(signSendEvent);
		return signSendEvent;
	}
}
