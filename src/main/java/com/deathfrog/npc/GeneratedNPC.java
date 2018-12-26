package com.deathfrog.npc;

import java.util.HashMap;

/**
 * Represents a fully generated NPC
 * TODO: Diety
 * TODO: Alignment
 * TODO: Make age optional
 * DONE: Pastable format
 * DONE: CMB / CMD
 * DONE: BAB
 * DONE: Hit Points
 * DONE: Civilian Classes
 * 
 * @author Al Mele
 *
 */
public class GeneratedNPC {
	protected NameDetails nameDetails = null;
	protected String gender = null;
	protected PathfinderClassDefinition pfClass = null;
	protected PathfinderRaceDefinition pfRace = null;
	protected String pattern = null;
	protected HashMap<EStat, Integer> stats = new HashMap<EStat, Integer>();
	protected Integer level = null;
	protected String deity = null;
	protected Integer hitpoints = null;
	
	/**
	 * @return the pfClass
	 */
	public PathfinderClassDefinition getPfClass() {
		return pfClass;
	}
	/**
	 * @param pfClass the pfClass to set
	 */
	public void setPfClass(PathfinderClassDefinition pfClass) {
		this.pfClass = pfClass;
	}
	/**
	 * @return the pfRace
	 */
	public PathfinderRaceDefinition getPfRace() {
		return pfRace;
	}
	public String getDeity() {
		return deity;
	}
	public void setDeity(String deity) {
		this.deity = deity;
	}
	/**
	 * @param pfRace the pfRace to set
	 */
	public void setPfRace(PathfinderRaceDefinition pfRace) {
		this.pfRace = pfRace;
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
	
	/**
	 * Returns the primary statistic of this Pathfinder class (null if the class is not set)
	 * 
	 * @return
	 */
	public EStat getPrimaryStat() {
		EStat[] prioritizedStats = this.getPfClass().getStatPriority();
		EStat stat = null;
		
		if (pfClass != null && prioritizedStats != null && prioritizedStats.length > 0) {
			stat = prioritizedStats[0];
		} else {
			// If no primary statistic is defined for the class in the configuration, assume strength.
			stat = EStat.STR;
		}
		
		return stat;
	}
	
	/**
	 * Returns the value of the most significant statistic for this class (requires class to be set)
	 * 
	 * @return
	 */
	public Integer getPrimaryStatValue() {
		Integer statvalue = null;
		
		if (pfClass != null) {
			EStat primary = getPrimaryStat();
			statvalue = getStatValue(primary);
		}
		
		return statvalue;
	}
	
	/**
	 * Get the value of a specific statistic
	 * 
	 * @param stat
	 * @return
	 */
	public Integer getStatValue(EStat stat) {
		Integer statvalue = stats.get(stat);
		
		return statvalue;
	}

	
	/**
	 * @return the stats
	 */
	public HashMap<EStat, Integer> getStats() {
		return stats;
	}
	
	/**
	 * Set a specific stat for the NPC.
	 */
	public void setStatValue(EStat stat, Integer statvalue) {
		stats.put(stat, statvalue);
	}
	
	/**
	 * @param stats the stats to set
	 */
	public void setStats(HashMap<EStat, Integer> stats) {
		this.stats = stats;
	}
	/**
	 * @return the name
	 */
	public NameDetails getName() {
		return nameDetails;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(NameDetails nameDetails) {
		this.nameDetails = nameDetails;
	}
	
	/**
	 * @return the gender
	 */
	public String getGender() {
		return gender;
	}
	/**
	 * @param gender the gender to set
	 */
	public void setGender(String gender) {
		this.gender = gender;
	}
	
	/**
	 * @return the hitpoints
	 */
	public Integer getHitpoints() {
		return hitpoints;
	}
	/**
	 * @param hitpoints the hitpoints to set
	 */
	public void setHitpoints(Integer hitpoints) {
		this.hitpoints = hitpoints;
	}
	/**
	 * @return the level
	 */
	public Integer getLevel() {
		return level;
	}
	/**
	 * @param level the level to set
	 */
	public void setLevel(Integer level) {
		this.level = level;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		sb.append(nameDetails.getName());
		
		if (pfRace != null) {
			sb.append(", ").append(pfRace.pickAge()).append(" year old ").append(gender).append(" ");
			sb.append(pfRace.getRace()).append(" ");
		} else {
			sb.append("Undefined race.");
		}
		
		if (pfClass != null) {
			sb.append(pfClass.getPathfinderClassName() + " ");
			
			if (level != null) {
				sb.append("Level ");
				sb.append(level);
				sb.append(" [BAB: ").append(pfClass.getBabForLevel(level));
				sb.append(", CMB: ").append(getCMB()).append(", CMD: ").append(getCMD());
				sb.append("]");
				sb.append(", HP: ");
				sb.append(hitpoints);
			}
		} else {
			sb.append("Undefined class.");
		}
		
		int i = 0;
		sb.append(" (");
		for (EStat stat : EStat.values()) {
			i++;
			sb.append(stat.getName().toUpperCase() + ": " + getStats().get(stat).toString());
			if (i < EStat.values().length) {
				 sb.append(", ");
			}
		}
		sb.append(")");
		
		// Add worship information.
		if (this.getDeity() != null && this.getDeity().length() > 0) {
			sb.append("  Deity: " );
			sb.append(this.getDeity());
		}
		
		
		return sb.toString();
	}
	
	// CMB = Base attack bonus + Strength modifier + special size modifier
	public int getCMB( ) {
		int bab = pfClass.getBabForLevel(level);
		int strModifier = EStat.bonus(this.getStatValue(EStat.STR));
		return (bab + strModifier + 0);
		
	}
	
	// CMD = 10 + Base attack bonus + Strength modifier + Dexterity modifier + special size modifier + miscellaneous modifiers
	public int getCMD() {
		int bab = pfClass.getBabForLevel(level);
		int strModifier = EStat.bonus(this.getStatValue(EStat.STR));
		int dexModifier = EStat.bonus(this.getStatValue(EStat.DEX));
		return (10 + bab + strModifier + dexModifier + 0);
	}
}
