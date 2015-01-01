package de.blablubbabc.insigns;

import com.comphenix.protocol.events.PacketContainer;

public class PacketUpdateSignWrapper {

	private final PacketContainer updateSignPacket;

	public PacketUpdateSignWrapper(PacketContainer updateSignPacket) {
		this.updateSignPacket = updateSignPacket;
	}

	public int getX() {
		return updateSignPacket.getIntegers().read(0).intValue();
	}

	public short getY() {
		return updateSignPacket.getIntegers().read(1).shortValue();
	}

	public int getZ() {
		return updateSignPacket.getIntegers().read(2).intValue();
	}

	public void setX(int value) {
		updateSignPacket.getIntegers().write(0, value);
	}

	public void setY(short value) {
		updateSignPacket.getIntegers().write(1, (int) value);
	}

	public void setZ(int value) {
		updateSignPacket.getIntegers().write(2, value);
	}

	public String[] getLines() {
		return updateSignPacket.getStringArrays().read(0);
	}

	public void setLines(String[] lines) {
		if (lines == null) throw new IllegalArgumentException("The lines array cannot be null!.");
		if (lines.length != 4) throw new IllegalArgumentException("The lines array must be four elements long.");
		updateSignPacket.getStringArrays().write(0, lines);
	}

	public PacketContainer getPacket() {
		return updateSignPacket;
	}
}
