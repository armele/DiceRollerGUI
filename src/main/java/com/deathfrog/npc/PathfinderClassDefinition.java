package com.deathfrog.npc;


/**
 * @author Al Mele
 *
 */
public class PathfinderClassDefinition {

	protected String pathfinderClassName; 
	
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

}
