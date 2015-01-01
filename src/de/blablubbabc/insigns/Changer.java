package de.blablubbabc.insigns;

import org.bukkit.Location;
import org.bukkit.entity.Player;

@Deprecated
public abstract class Changer {

	private final String key;
	private final String perm;

	/**
	 * Creates a new Changer. A Changer is used to specify which text should be replaced with what
	 * other text on signs. Additional it holds a permission node for which is automatically checked
	 * on sign creation.
	 * 
	 * @param key
	 *            This will later be replaced on the signs.
	 * @param permission
	 *            This is the permissions node a player needs to be able to create a sign with the
	 *            key on it.
	 */
	@Deprecated
	public Changer(String key, String permission) {
		if (key == null || permission == null) {
			throw new IllegalArgumentException("The key and the permissions node must not be null!");
		}

		this.key = key;
		this.perm = permission;
	}

	/**
	 * Get the key which will later be replaced on signs.
	 * 
	 * @return The text which will get replaced.
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Returns the permission node which is need to create a sign with the key of this Changer on
	 * it.
	 * 
	 * @return The needed permissions node.
	 */
	public String getPerm() {
		return perm;
	}

	/**
	 * Get the value which replaces the key.
	 * 
	 * @param player
	 *            The player looking at the sign.
	 * @return The text which replaces the text on the sign.
	 */
	public abstract String getValue(final Player player, final Location location);

}
