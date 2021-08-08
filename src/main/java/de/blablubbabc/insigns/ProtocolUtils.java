/**
 * Copyright (c) blablubbabc <http://www.blablubbabc.de>
 * All rights reserved.
 */
package de.blablubbabc.insigns;

import java.util.List;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
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

		public static class MapChunk {

			private MapChunk() {
			}

			public static boolean isMapChunkPacket(PacketContainer packet) {
				if (packet == null) return false;
				if (packet.getType() != PacketType.Play.Server.MAP_CHUNK) return false;
				return true;
			}

			@SuppressWarnings("unchecked")
			public static List<Object> getTileEntitiesData(PacketContainer packet) {
				assert isMapChunkPacket(packet);
				return packet.getSpecificModifier(List.class).read(0);
			}

			public static void setTileEntitiesData(PacketContainer packet, List<Object> tileEntitiesData) {
				assert isMapChunkPacket(packet);
				packet.getSpecificModifier(List.class).write(0, tileEntitiesData);
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
				String id = getId(tileEntityData);
				return id.equals("minecraft:sign");
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
