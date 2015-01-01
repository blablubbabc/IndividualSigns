package de.blablubbabc.insigns;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.WrappedChatComponent;

public class UpdateSignPacketUtility {

	private UpdateSignPacketUtility() {
	}

	public static BlockPosition getLocation(PacketContainer updateSignPacket) {
		return updateSignPacket.getBlockPositionModifier().read(0);
	}

	public static void setLocation(PacketContainer updateSignPacket, BlockPosition blockPosition) {
		updateSignPacket.getBlockPositionModifier().write(0, blockPosition);
	}

	public static WrappedChatComponent[] getLines(PacketContainer updateSignPacket) {
		return updateSignPacket.getChatComponentArrays().read(0);
	}

	public static void setLines(PacketContainer updateSignPacket, WrappedChatComponent[] lines) {
		assert lines != null;
		assert lines.length == 4;

		updateSignPacket.getChatComponentArrays().write(0, lines);
	}

	public static String[] getLinesAsStrings(PacketContainer updateSignPacket) {
		String[] lines = new String[4];
		WrappedChatComponent[] rawLines = getLines(updateSignPacket);
		for (int i = 0; i < 4; i++) {
			lines[i] = rawLines[i].getJson();
		}
		return lines;
	}

	public static void setLinesFromStrings(PacketContainer updateSignPacket, String[] lines) {
		assert lines != null;
		assert lines.length == 4;

		WrappedChatComponent[] rawLines = new WrappedChatComponent[4];
		for (int i = 0; i < 4; i++) {
			rawLines[i] = WrappedChatComponent.fromJson(lines[i]);
		}

		setLines(updateSignPacket, rawLines);
	}
}
