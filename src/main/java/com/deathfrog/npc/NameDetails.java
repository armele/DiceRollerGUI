package com.deathfrog.npc;

/**
 * @author Al Mele
 *
 */
public class NameDetails {
	protected String name;
	protected String pattern;
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @return the pattern
	 */
	public String getPattern() {
		return pattern;
	}
	
	/**
	 * @param pattern the pattern to set
	 */
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "NameDetails [name=" + name + ", pattern=" + pattern + "]";
	}
	
}
