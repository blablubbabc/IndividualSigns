package de.blablubbabc.insigns;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.plugin.Plugin;

public abstract class SimpleChanger implements Listener {

	private final String key;
	private final String permissionsNode;

	public SimpleChanger(Plugin plugin, String key, String permissionsNode) {
		if (plugin == null || !plugin.isEnabled()) {
			throw new IllegalArgumentException("The plugin must not be null and has to be enabled!");
		}
		if ((key == null) || (permissionsNode == null)) {
			throw new IllegalArgumentException("The key and the permissions node must not be null!");
		}
		if (key.length() > 15) {
			throw new IllegalArgumentException("The key must not be longer then 15!");
		}
		this.key = key;
		this.permissionsNode = permissionsNode;

		Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
	}

	public abstract String getValue(Player player, Location location, String originalLine);

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	void onSignSend(SignSendEvent event) {
		for (int i = 0; i < 4; i++) {
			String line = event.getLine(i);
			if (line.contains(key)) {
				event.setLine(i, line.replace(key, this.getValue(event.getPlayer(), event.getLocation(), line)));
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	void onSignCreate(SignChangeEvent event) {
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
