package com.deathfrog.npc;

public class GenerationConfig {
	protected String fixedClass = null;
	protected String fixedRace = null;
	protected Integer minLevel = null;
	protected Integer maxLevel = null;
	
	/**
	 * @return the fixedClass
	 */
	public String getFixedClass() {
		return fixedClass;
	}
	/**
	 * @param fixedClass the fixedClass to set
	 */
	public void setFixedClass(String fixedClass) {
		this.fixedClass = fixedClass;
	}
	/**
	 * @return the fixedRace
	 */
	public String getFixedRace() {
		return fixedRace;
	}
	/**
	 * @param fixedRace the fixedRace to set
	 */
	public void setFixedRace(String fixedRace) {
		this.fixedRace = fixedRace;
	}
	/**
	 * @return the minLevel
	 */
	public Integer getMinLevel() {
		return minLevel;
	}
	/**
	 * @param minLevel the minLevel to set
	 */
	public void setMinLevel(Integer minLevel) {
		this.minLevel = minLevel;
	}
	/**
	 * @return the maxLevel
	 */
	public Integer getMaxLevel() {
		return maxLevel;
	}
	/**
	 * @param maxLevel the maxLevel to set
	 */
	public void setMaxLevel(Integer maxLevel) {
		this.maxLevel = maxLevel;
	}
	
	/**
	 * Indicates whether or not the class has been set to a fixed value.
	 * 
	 * @return
	 */
	public boolean isClassOverride() {
		boolean ico = false;
		
		if (fixedClass != null && fixedClass.length() > 0) {
			ico = true;
		}
		
		return ico;
	}
	
	
	/**
	 * Indicates whether or not the race has been set to a fixed value.
	 * 
	 * @return
	 */
	public boolean isRaceOverride() {
		boolean iro = false;
		
		if (fixedRace != null && fixedRace.length() > 0) {
			iro = true;
		}
		
		return iro;
	}	
}
