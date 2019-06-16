/**
 * Copyright (c) blablubbabc <http://www.blablubbabc.de>
 * All rights reserved.
 */
package de.blablubbabc.insigns;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;

public class Utils {

	private Utils() {
	}

	private final static EnumSet<Material> SIGNS = EnumSet.noneOf(Material.class);

	static {
		addSignMaterialByName("SIGN");
		addSignMaterialByName("WALL_SIGN");
		addSignMaterialByName("OAK_SIGN");
		addSignMaterialByName("OAK_WALL_SIGN");
		addSignMaterialByName("BIRCH_SIGN");
		addSignMaterialByName("BIRCH_WALL_SIGN");
		addSignMaterialByName("SPRUCE_SIGN");
		addSignMaterialByName("SPRUCE_WALL_SIGN");
		addSignMaterialByName("JUNGLE_SIGN");
		addSignMaterialByName("JUNGLE_WALL_SIGN");
		addSignMaterialByName("DARK_OAK_SIGN");
		addSignMaterialByName("DARK_OAK_WALL_SIGN");
		addSignMaterialByName("ACACIA_SIGN");
		addSignMaterialByName("ACACIA_WALL_SIGN");
	}

	private static void addSignMaterialByName(String materialName) {
		Material material = Material.getMaterial(materialName);
		if (material != null) {
			SIGNS.add(material);
		}
	}

	public static boolean isSign(Material material) {
		return SIGNS.contains(material);
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
