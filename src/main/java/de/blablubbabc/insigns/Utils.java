/**
 * Copyright (c) blablubbabc <http://www.blablubbabc.de>
 * All rights reserved.
 */
package de.blablubbabc.insigns;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.Chunk;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;

/**
 * General utilities.
 */
public final class Utils {

	private Utils() {
	}

	/**
	 * Checks if the given {@link Material} is a sign.
	 * 
	 * @param material
	 *            the material, not <code>null</code>
	 * @return <code>true</code> if the material is a sign
	 */
	public static boolean isSign(Material material) {
		Validate.notNull(material, "material");
		return (material.data == org.bukkit.block.data.type.Sign.class)
				|| (material.data == org.bukkit.block.data.type.WallSign.class);
	}

	/**
	 * Gets the sign's text color, or the default {@link DyeColor#BLACK} if no text color is explicitly set for the
	 * sign.
	 * 
	 * @param sign
	 *            the sign
	 * @return the sign's text color, not <code>null</code>
	 */
	public static DyeColor getSignTextColor(org.bukkit.block.Sign sign) {
		Validate.notNull(sign, "sign");
		DyeColor color = sign.getColor(); // Can be null
		return (color != null) ? color : DyeColor.BLACK; // Default: Black
	}

	/**
	 * Sends the a sign update packet for the given sign to the specified player.
	 * 
	 * @param player
	 *            the player, not <code>null</code>
	 * @param sign
	 *            the sign, not <code>null</code>
	 */
	public static void sendSignUpdate(Player player, org.bukkit.block.Sign sign) {
		Validate.notNull(player, "player");
		Validate.notNull(sign, "sign");
		player.sendSignChange(sign.getLocation(), sign.getLines(), getSignTextColor(sign), sign.isGlowingText());
	}

	/**
	 * Gets the {@link BlockState BlockStates} for tile entities of a certain type in a certain chunk radius.
	 * <p>
	 * This method only takes chunks into account that are currently loaded.
	 * 
	 * @param <T>
	 *            only block states of this type are returned
	 * @param location
	 *            the center location, not <code>null</code>
	 * @param chunkRadius
	 *            the radius in chunks in which to check for tile entities, not negative, a chunk radius of {@code 0}
	 *            checks a single chunk
	 * @param type
	 *            only block states of this type are returned
	 * @return the found block states, not <code>null</code>, but can be empty
	 */
	public static <T extends BlockState> List<T> getNearbyTileEntities(Location location, int chunkRadius, Class<T> type) {
		Validate.notNull(location, "location");
		World world = location.getWorld();
		Validate.notNull(world, "The location's world is null!");
		Validate.notNull(type, "type");
		Validate.isTrue(chunkRadius >= 0, "chunkRadius cannot be negative");

		List<T> tileEntities = new ArrayList<>();
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
