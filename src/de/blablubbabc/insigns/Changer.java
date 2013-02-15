package de.blablubbabc.insigns;

public abstract class Changer {

	private final String key;

	/**
	 * Creates a new Changer. A Changer is used to specify which text should be
	 * replaced with what other text on signs.
	 * 
	 * @param key
	 *            This will later be replaced on the signs.
	 */
	public Changer(String key) {
		this.key = key;
	}

	/**
	 * Get the key which will later be replaced on signs.
	 * 
	 * @return the text which will get replaced
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Get the value which replaces the key.
	 * 
	 * @param playerName
	 *            the name of the player looking at the sign
	 * @return the text which replaces the text on the sign
	 */
	public abstract String getValue(String playerName);

}
