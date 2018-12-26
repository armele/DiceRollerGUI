package com.deathfrog.npc;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class stores a global map of race and class definitions that can be used across contexts.
 * 
 * @author Al Mele
 *
 */
public class NpcDefinitions extends ArrayList<NpcContext> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected static HashMap<String, NameGenerator> nameGenMap = new HashMap<String, NameGenerator>();
	protected static HashMap<String, PathfinderRaceDefinition> globalRaceMap = new HashMap<String, PathfinderRaceDefinition>();
	protected static HashMap<String, PathfinderClassDefinition> globalClassMap = new HashMap<String, PathfinderClassDefinition>();
	
	/**
	 * @param mapname
	 * @param gen
	 */
	public static void addNameGen(String generatorName, NameGenerator gen) {
		nameGenMap.put(generatorName, gen);
	}
	
	/**
	 * @param generatorName
	 * @return
	 */
	public static NameGenerator getNameGen(String generatorName) {
		return nameGenMap.get(generatorName);
	}
	
	/**
	 * @param name
	 * @param pfRace
	 */
	public static void addRace(String name, PathfinderRaceDefinition pfRace) {
		globalRaceMap.put(name, pfRace);
	}
	
	/**
	 * @param name
	 * @return
	 */
	public static PathfinderRaceDefinition getRace(String name) {
		return globalRaceMap.get(name);
	}
	
	/**
	 * @param name
	 * @param pfClass
	 */
	public static void addClass(String name, PathfinderClassDefinition pfClass) {
		globalClassMap.put(name, pfClass);
	}
	
	/**
	 * @param name
	 * @return
	 */
	public static PathfinderClassDefinition getClass(String name) {
		return globalClassMap.get(name);
	}	
}
 