package com.deathfrog.npc;

public enum EStat {
	STR ("str"),
	CON ("con"),
	DEX ("dex"),
	INT ("int"),
	WIS ("wis"),
	CHA ("cha");
	
	EStat(String name) {
		this.name = name;
	}
	
	protected String name;
	
	public String getName() {
		return name;
	}
	
	/**
	 * Indicates whether a given name represents a valid Pathfinder statistic.
	 * Handy for XML parsing logic.
	 * 
	 * @param statname
	 * @return
	 */
	static public boolean validStat(String statname) {
		boolean valid = false;
		
		for (EStat stat : values()) {
			if (stat.getName().equals(statname)) {
				valid = true;
				break;
			}
		}
		
		return valid;
	}
	
	/**
	 * Given a string representation of a statistic, return the corresponding enumeration.
	 * 
	 * @return
	 */
	static public EStat statForString(String statname) {	
		EStat stat = null;
		
		for (EStat s : values()) {
			if (s.getName().equals(statname)) {
				stat = s;
				break;
			}
		}
		
		return stat;		
	}
}
