package com.deathfrog.npc;

/**
 * @author Al Mele
 *
 */
public class PathfinderClassDefinition {

	protected String pathfinderClassName; 
	protected EStat[] statOrder = new EStat[6];
	
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
		statOrder[statspot] = stat;
	}
	
	/**
	 * @return
	 */
	public EStat[] getStatPriority() {
		return statOrder;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getPathfinderClassName();
	}
	
	
}
