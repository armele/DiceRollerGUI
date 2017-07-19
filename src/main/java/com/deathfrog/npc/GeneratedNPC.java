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
	 * @return the stats
	 */
	public HashMap<EStat, Integer> getStats() {
		return stats;
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
