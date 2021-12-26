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

			public static final int UPDATE_SIGN_ACTION_ID = 9;

			public static boolean isUpdateSignPacket(PacketContainer packet) {
				assert isTileEntityDataPacket(packet);
				int actionId = packet.getIntegers().read(0);
				if (actionId != UPDATE_SIGN_ACTION_ID) return false;
				return true;
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

		@SuppressWarnings("unchecked")
		public static class MapChunk {

			private static final EquivalentConverter<InternalStructure> TILE_ENTITY_CONVERTER;
			private static Constructor<?> tile_entity_constructor; // lazy initialisation
			static {
				try {
					Field converterField = InternalStructure.class.getDeclaredField("CONVERTER");
					converterField.setAccessible(true);
					TILE_ENTITY_CONVERTER = (EquivalentConverter<InternalStructure>) converterField.get(null);
				} catch (ReflectiveOperationException e) {
					throw new RuntimeException("Could not get the tile entity converter", e);
				}
			}

			private MapChunk() {
			}

			public static boolean isMapChunkPacket(PacketContainer packet) {
				if (packet == null) return false;
				if (packet.getType() != PacketType.Play.Server.MAP_CHUNK) return false;
				return true;
			}

			public static List<InternalStructure> getTileEntitiesData(PacketContainer packet) {
				assert isMapChunkPacket(packet);
				return packet.getStructures().read(0).getLists(TILE_ENTITY_CONVERTER).read(0);
			}

			public static void setTileEntitiesData(PacketContainer packet, List<InternalStructure> tileEntitiesData) {
				assert isMapChunkPacket(packet);
				packet.getStructures().read(0).getLists(TILE_ENTITY_CONVERTER).write(0, tileEntitiesData);
			}

			public static int getX(PacketContainer packet) {
				return  packet.getIntegers().read(0);
			}

			public static int getZ(PacketContainer packet) {
				return  packet.getIntegers().read(1);
			}

			public static NbtBase<?> getTileEntityNBT(InternalStructure tileEntityData) {
				return tileEntityData.getNbtModifier().read(0);
			}

			public static int getTileEntityLocalX(InternalStructure tileEntityData) {
				return  ((tileEntityData.getIntegers().read(0) >> 4) & 0xf);
			}

			public static int getTileEntityLocalZ(InternalStructure tileEntityData) {
				return  (tileEntityData.getIntegers().read(0) & 0xf);
			}

			public static int getTileEntityY(InternalStructure tileEntityData) {
				return tileEntityData.getIntegers().read(1);
			}

			public static InternalStructure cloneTileEntityDataWithNewNbt(InternalStructure tileEntityData, NbtCompound nbt) {
				if (tile_entity_constructor == null) {
					for (Constructor<?> c : tileEntityData.getHandle().getClass().getDeclaredConstructors()) {
						if (c.getParameterCount() == 4) { // we are looking for the only constructor with 4 params
							c.setAccessible(true);
							tile_entity_constructor = c;
							break;
						}
					}
					if (tile_entity_constructor == null) {
						throw new RuntimeException("Could not find the tile entity constructor");
					}
				}
				try {
					Object instance = tile_entity_constructor.newInstance(tileEntityData.getModifier().read(0), tileEntityData.getModifier().read(1), tileEntityData.getModifier().read(2), nbt.getHandle());
					return TILE_ENTITY_CONVERTER.getSpecific(instance);
				} catch (ReflectiveOperationException e) {
					throw new RuntimeException("Could not invoke the tile entity constructor", e);
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
