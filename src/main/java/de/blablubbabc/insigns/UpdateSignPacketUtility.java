package de.blablubbabc.insigns;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;

public class UpdateSignPacketUtility {

	private UpdateSignPacketUtility() {
	}

	public static final int UPDATE_SIGN_ACTION_ID = 9;

	public static boolean isUpdateSignPacket(PacketContainer tileEntityDataPacket) {
		int actionId = tileEntityDataPacket.getIntegers().read(0);
		return actionId == UPDATE_SIGN_ACTION_ID;
	}

	public static BlockPosition getLocation(PacketContainer tileEntityDataPacket) {
		return tileEntityDataPacket.getBlockPositionModifier().read(0);
	}

	public static void setLocation(PacketContainer tileEntityDataPacket, BlockPosition blockPosition) {
		tileEntityDataPacket.getBlockPositionModifier().write(0, blockPosition);
	}

	public static NbtCompound getTileEntityData(PacketContainer tileEntityDataPacket) {
		return (NbtCompound) tileEntityDataPacket.getNbtModifier().read(0);
	}

	public static String[] getRawLines(PacketContainer updateSignPacket) {
		NbtCompound data = getTileEntityData(updateSignPacket);
		String[] rawLines = new String[4];
		for (int i = 0; i < 4; i++) {
			String rawLine = data.getString("Text" + (i + 1));
			rawLines[i] = rawLine == null ? "" : rawLine;
		}
		return rawLines;
	}

	public static void setRawLines(PacketContainer updateSignPacket, String[] lines) {
		assert lines != null;
		assert lines.length == 4;

		NbtCompound data = getTileEntityData(updateSignPacket);
		for (int i = 0; i < 4; i++) {
			data.put("Text" + (i + 1), lines[i]);
		}
	}
}
