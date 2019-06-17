/**
 * Copyright (c) blablubbabc <http://www.blablubbabc.de>
 * All rights reserved.
 */
package de.blablubbabc.insigns;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;

public class Utils {

	private Utils() {
	}

	public static boolean isSign(Material material) {
		if (material == null) return false;
		return (material.data == org.bukkit.block.data.type.Sign.class)
				|| (material.data == org.bukkit.block.data.type.WallSign.class);
	}

	public static <T extends BlockState> List<T> getNearbyTileEntities(Location location, int chunkRadius, Class<T> type) {
		if (location == null || location.getWorld() == null || chunkRadius <= 0 || type == null) return Collections.emptyList();
		List<T> tileEntities = new ArrayList<>();
		World world = location.getWorld();
		Chunk center = location.getChunk();
		int startX = center.getX() - chunkRadius;
		int endX = center.getX() + chunkRadius;
		int startZ = center.getZ() - chunkRadius;
		int endZ = center.getZ() + chunkRadius;
		for (int chunkX = startX; chunkX <= endX; chunkX++) {
			for (int chunkZ = startZ; chunkZ <= endZ; chunkZ++) {
				if (!world.isChunkLoaded(chunkX, chunkZ)) continue;
				Chunk chunk = world.getChunkAt(chunkX, chunkZ);
				for (BlockState tileEntity : chunk.getTileEntities()) {
					if (tileEntity == null) continue;
					if (type.isInstance(tileEntity)) {
						tileEntities.add(type.cast(tileEntity));
					}
				}
			}
		}
		return tileEntities;
	}
}
