package com.deathfrog.npc;

/**
 * @author Al Mele
 *
 */
public class PathfinderClassDefinition {

	protected String pathfinderClassName; 
	protected Integer hitdice = null;
	protected EStat[] statOrder = null;
	protected int[] babProgression = new int[20];
	
	public PathfinderClassDefinition(String pathfinderClassName) {
		this.pathfinderClassName = pathfinderClassName;
	}

	/**
	 * @return the pathfinderClassName
	 */
	public String getPathfinderClassName() {
		return pathfinderClassName;
	}


	/**
	 * @param pathfinderClassName the pathfinderClassName to set
	 */
	public void setPathfinderClassName(String pathfinderClassName) {
		this.pathfinderClassName = pathfinderClassName;
	}

	/**
	 * Given a stat priority order, return the tag for the statistic at that order location.
	 * 
	 * @param statspot
	 * @return
	 */
	public EStat getStatPriority(int statspot) {
		EStat stat = null;
		
		if (statOrder != null) {
			stat = statOrder[statspot];
		}
		
		return stat;
	}
	
	/**
	 * @param statspot
	 * @param stat
	 */
	public void setStatPriority(int statspot, EStat stat) {
		if (statOrder == null) {
			statOrder = new EStat[6];
		}
		statOrder[statspot] = stat;
	}
	
	/**
	 * @return
	 */
	public EStat[] getStatPriority() {
		return statOrder;
	}
	
	
	/**
	 * @return the hitdice
	 */
	public Integer getHitdice() {
		return hitdice;
	}

	/**
	 * @param hitdice the hitdice to set
	 */
	public void setHitdice(Integer hitdice) {
		this.hitdice = hitdice;
	} 
	
	/**
	 * @param level
	 * @return
	 */
	public int getBabForLevel(int level) {
		return babProgression[level-1];
	}
	
	/**
	 * @param level
	 * @param bab
	 */
	public void setBabForLevel(int level, int bab) {
		babProgression[level-1] = bab;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getPathfinderClassName();
	}
	
	
}
