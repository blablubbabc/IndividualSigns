/**
 * Copyright (c) blablubbabc <http://www.blablubbabc.de>
 * All rights reserved.
 */
package de.blablubbabc.insigns;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.InternalStructure;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.nbt.NbtBase;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;

/**
 * Utilities for reading and writing packet contents.
 */
public class ProtocolUtils {

	private ProtocolUtils() {
	}

	public static class Packet {

		private Packet() {
		}

		public static class TileEntityData {

			private TileEntityData() {
			}

			public static boolean isTileEntityDataPacket(PacketContainer packet) {
				if (packet == null) return false;
				if (packet.getType() != PacketType.Play.Server.TILE_ENTITY_DATA) return false;
				return true;
			}

			// TODO Update this to use the packet's BlockEntityType field to identify sign updates. However, ProtocolLib
			// does not seem to offer some kind of converter for BlockEntityType yet (e.g. one that maps to a BlockState
			// class, or some other closest Bukkit representation).
			@Deprecated
			public static boolean isUpdateSignPacket(PacketContainer packet) {
				assert isTileEntityDataPacket(packet);
				throw new UnsupportedOperationException("Outdated operation");
			}

			public static BlockPosition getBlockPosition(PacketContainer packet) {
				assert isTileEntityDataPacket(packet);
				return packet.getBlockPositionModifier().read(0);
			}

			public static void setBlockPosition(PacketContainer packet, BlockPosition blockPosition) {
				assert isTileEntityDataPacket(packet);
				packet.getBlockPositionModifier().write(0, blockPosition);
			}

			public static NbtCompound getTileEntityData(PacketContainer packet) {
				assert isTileEntityDataPacket(packet);
				return (NbtCompound) packet.getNbtModifier().read(0);
			}

			public static void setTileEntityData(PacketContainer packet, NbtCompound tileEntityData) {
				assert isTileEntityDataPacket(packet);
				packet.getNbtModifier().write(0, tileEntityData);
			}
		}

		public static class MapChunk {

			private MapChunk() {
			}

			public static boolean isMapChunkPacket(PacketContainer packet) {
				if (packet == null) return false;
				if (packet.getType() != PacketType.Play.Server.MAP_CHUNK) return false;
				return true;
			}

			public static List<InternalStructure> getTileEntitiesInfo(PacketContainer packet) {
				assert isMapChunkPacket(packet);
				return packet.getStructures().read(0).getLists(TileEntityInfo.CONVERTER).read(0);
			}

			public static void setTileEntitiesInfo(PacketContainer packet, List<InternalStructure> tileEntitiesInfo) {
				assert isMapChunkPacket(packet);
				packet.getStructures().read(0).getLists(TileEntityInfo.CONVERTER).write(0, tileEntitiesInfo);
			}

			public static int getChunkX(PacketContainer packet) {
				return packet.getIntegers().read(0);
			}

			public static int getChunkZ(PacketContainer packet) {
				return packet.getIntegers().read(1);
			}

			@SuppressWarnings("unchecked")
			public static class TileEntityInfo {

				private static final EquivalentConverter<InternalStructure> CONVERTER;
				static {
					try {
						Field converterField = InternalStructure.class.getDeclaredField("CONVERTER");
						converterField.setAccessible(true);
						CONVERTER = (EquivalentConverter<InternalStructure>) converterField.get(null);
					} catch (ReflectiveOperationException e) {
						throw new RuntimeException("Could not get the tile entity info converter!", e);
					}
				}

				private static Constructor<?> CONSTRUCTOR; // Lazily initialized

				private TileEntityInfo() {
				}

				// The x and y coordinates are stored compactly as: ((X & 15) << 4) | (Z & 15)
				private static int getPackedXZ(InternalStructure tileEntityInfo) {
					return tileEntityInfo.getIntegers().read(0);
				}

				public static int getLocalX(InternalStructure tileEntityInfo) {
					return (getPackedXZ(tileEntityInfo) >> 4);
				}

				public static int getLocalZ(InternalStructure tileEntityInfo) {
					return (getPackedXZ(tileEntityInfo) & 0xf);
				}

				public static int getY(InternalStructure tileEntityInfo) {
					return tileEntityInfo.getIntegers().read(1);
				}

				private static Object getTileEntityType(InternalStructure tileEntityInfo) {
					return tileEntityInfo.getModifier().read(2);
				}

				// Can be null
				public static NbtCompound getNbt(InternalStructure tileEntityInfo) {
					NbtBase<?> nbt = tileEntityInfo.getNbtModifier().read(0);
					if (nbt == null) return null;
					return NbtFactory.asCompound(nbt);
				}

				public static InternalStructure cloneWithNewNbt(InternalStructure tileEntityInfo, NbtCompound nbt) {
					if (CONSTRUCTOR == null) {
						for (Constructor<?> constructor : tileEntityInfo.getHandle().getClass().getDeclaredConstructors()) {
							// We are looking for the only constructor with 4 parameters:
							if (constructor.getParameterCount() == 4) {
								constructor.setAccessible(true);
								CONSTRUCTOR = constructor;
								break;
							}
						}
						if (CONSTRUCTOR == null) {
							throw new RuntimeException("Could not find the tile entity info constructor!");
						}
					}

					Object packedXZ = getPackedXZ(tileEntityInfo);
					Object y = getY(tileEntityInfo);
					Object tileEntityType = getTileEntityType(tileEntityInfo);
					Object nmsNbt = nbt.getHandle();

					try {
						Object instance = CONSTRUCTOR.newInstance(packedXZ, y, tileEntityType, nmsNbt);
						return CONVERTER.getSpecific(instance);
					} catch (ReflectiveOperationException e) {
						throw new RuntimeException("Could not invoke the tile entity info constructor!", e);
					}
				}
			}
		}
	}

	public static class TileEntity {

		private TileEntity() {
		}

		public static String getId(NbtCompound tileEntityData) {
			assert tileEntityData != null;
			return tileEntityData.getString("id");
		}

		public static int getX(NbtCompound tileEntityData) {
			assert tileEntityData != null;
			return tileEntityData.getInteger("x");
		}

		public static int getY(NbtCompound tileEntityData) {
			assert tileEntityData != null;
			return tileEntityData.getInteger("y");
		}

		public static int getZ(NbtCompound tileEntityData) {
			assert tileEntityData != null;
			return tileEntityData.getInteger("z");
		}

		public static class Sign {

			private Sign() {
			}

			public static boolean isTileEntitySignData(NbtCompound tileEntityData) {
				// TODO Some packets omit the TileEntity type id from the NBT data and instead store it separately in
				// form of a BlockEntityType. Once ProtocolLib offers some kind of converter to represent
				// BlockEntityType, all checks for whether some packet's tile entity data is a sign should be based on
				// the BlockEntityType, and not the NBT data.
				// String id = getId(tileEntityData);
				// return id.equals("minecraft:sign");
				return tileEntityData.containsKey("GlowingText");
			}

			public static String[] getText(NbtCompound tileEntitySignData) {
				assert tileEntitySignData != null;
				String[] lines = new String[4];
				for (int i = 0; i < 4; i++) {
					String rawLine = tileEntitySignData.getString("Text" + (i + 1));
					lines[i] = rawLine == null ? "" : rawLine;
				}
				return lines;
			}

			public static void setText(NbtCompound tileEntitySignData, String[] lines) {
				assert tileEntitySignData != null;
				assert lines != null;
				assert lines.length == 4;
				for (int i = 0; i < 4; i++) {
					tileEntitySignData.put("Text" + (i + 1), lines[i]);
				}
			}
		}
	}
}
