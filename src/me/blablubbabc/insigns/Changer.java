package me.blablubbabc.insigns;

public abstract class Changer {
	
	private final String key;
	private final String perm;
	
	/**
	 * Creates a new Changer.
	 * The changer object is used to define which text shall be replaced with what on the signs.
	 * 
	 * @param key This will later be replaced on the signs.
	 * @param perm This will be use for the permissions-check on sign creation. Node later: 'insigns.create.perm'
	 */
	public Changer(String key, String perm) {
		this.key = key;
		this.perm = perm;
	}
	
	public String getPerm() {
		return perm;
	}
	
	/**
	 * Get the key which will later be replaced on the signs
	 * 
	 * @return the key
	 */
	public String getKey() {
		return key;
	}
	
	/**
	 * Get the value which replaces the key.
	 *  
	 * @param playerName The name of the player looking at the sign.
	 * @return the value
	 */
	public abstract String getValue(String playerName);

}
