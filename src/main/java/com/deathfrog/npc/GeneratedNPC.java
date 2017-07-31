package com.deathfrog.npc;

import java.util.HashMap;

import org.eclipse.swt.widgets.Text;

/**
 * Represents a fully generated NPC
 * 
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
		EStat stat = null;
		
		if (pfClass != null) {
			stat = this.getPfClass().getStatPriority()[0];
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
			if (level != null) {
				sb.append("Level ");
				sb.append(level);
				sb.append(" ");
			}
			sb.append(pfClass.getPathfinderClassName() + " ");
		} else {
			sb.append("Undefined class.");
		}
		
		sb.append(Text.DELIMITER);				
		for (EStat stat : EStat.values()) {
			sb.append(stat.getName().toUpperCase() + ": " + getStats().get(stat).toString() + Text.DELIMITER);
		}
		
		return sb.toString();
	}
}
