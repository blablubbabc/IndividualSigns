/**
 * Copyright (c) blablubbabc <http://www.blablubbabc.de>
 * All rights reserved.
 */
package de.blablubbabc.insigns;

import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;

class PlayerListener implements Listener {

	private final InSigns plugin;

	PlayerListener(InSigns plugin) {
		Validate.notNull(plugin, "plugin");
		this.plugin = plugin;
	}

	// Allow players to manually trigger a sign update by right-clicking a sign.
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	void onPlayerInteract(PlayerInteractEvent event) {
		// Ignore off-hand interactions:
		if (event.getHand() != EquipmentSlot.HAND) return;

		if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
			Block block = event.getClickedBlock();
			Material blockType = block.getType();
			if (Utils.isSign(blockType)) {
				Player player = event.getPlayer();
				Sign sign = (Sign) block.getState();
				Utils.sendSignUpdate(player, sign);
			}
		}
	}

	// Update nearby signs a short delay after a player joined.
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		int updateDelay = plugin.getPlayerJoinSignUpdateDelay();
		plugin.updateNearbySignsDelayed(player, updateDelay);
	}
}
