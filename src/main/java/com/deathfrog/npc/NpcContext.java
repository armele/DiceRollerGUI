package com.deathfrog.npc;

import java.util.Arrays;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.deathfrog.utils.GameException;
import com.deathfrog.utils.PercentileList;
import com.deathfrog.utils.dice.Dice;

/**
 * Saves the name of the context as well as any associated percentile lists specific to that context.
 * 
 * You can think of the "context" as the adventure world from which the character will be generated.
 * 
 * @author Al Mele
 *
 */
public class NpcContext implements Comparable<NpcContext> {
	protected static Logger log = LogManager.getLogger(NpcContext.class);
	public static final String RACE = "Race";
	public static final String DEITY = "Deity";
	public static final String CLASS = "Class";
	public static final String LEVEL = "Level";
	public static final String STATPRIORITY = "statpriority";
	
	protected String name;
	protected HashMap<String, PercentileList<?>> contextLists = new HashMap<String, PercentileList<?>>();
	protected HashMap<String, PathfinderClassDefinition> classMap = new HashMap<String, PathfinderClassDefinition>();
	protected HashMap<String, PathfinderRaceDefinition> raceMap = new HashMap<String, PathfinderRaceDefinition>();
	
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
	 * @return the contextLists
	 */
	public HashMap<String, PercentileList<?>> getContextLists() {
		return contextLists;
	}
	/**
	 * @param contextLists the contextLists to set
	 */
	public void addContextList(String name, PercentileList<?> contextList) {
		contextLists.put(name, contextList);
	}
	
	/**
	 * @param className
	 * @param pfClass
	 */
	public void addClass(String className, PathfinderClassDefinition pfClass) {
		classMap.put(className, pfClass);
	}
	
	/**
	 * @param className
	 * @param pfClass
	 */
	public void addRace(String raceName, PathfinderRaceDefinition pfRace) {
		raceMap.put(raceName, pfRace);
	}
	
	/**
	 * @param className
	 * @param pfClass
	 */
	public PathfinderClassDefinition getPathfinderClass(String className) {
		PathfinderClassDefinition pfClass = classMap.get(className);
		
		if (pfClass == null) {
			pfClass = NpcDefinitions.getClass(className);
		}
		
		return pfClass;
	}
	
	/**
	 * @param className
	 * @param pfClass
	 */
	public PathfinderRaceDefinition getRace(String raceName) {
		PathfinderRaceDefinition pfRace = raceMap.get(raceName);
		
		if (pfRace == null) {
			pfRace = NpcDefinitions.getRace(raceName);
		}
		
		return pfRace;
	}
	
	
	/**
	 * Randomly determines the level of the NPC, and 
	 * 
	 * @param npc
	 */
	protected void determineLevelBasedItems(GeneratedNPC npc) {
		@SuppressWarnings("unchecked")
		PercentileList<String> lvlChance = (PercentileList<String>) contextLists.get(LEVEL);
		
		if (lvlChance != null) {
			Integer level = new Integer(lvlChance.pick());
			npc.setLevel(level);
			int levelbonus = level.intValue() / 4;
			npc.setStatValue(npc.getPrimaryStat(), npc.getPrimaryStatValue() + levelbonus);
			
			// Roll up hit points using the appropriate hit dice for the class.
			if (npc.getPfClass().getHitdice() != null) {
				Dice dicebag = new Dice(level, npc.getPfClass().getHitdice());
				dicebag.rollAll();
				int hp = dicebag.total(false);
				hp = hp + (level * EStat.bonus(npc.getStatValue(EStat.CON)));
				npc.setHitpoints(hp);
			}
		}
	}
	
	/**
	 * Provided the NPC has already had a class and race picked for it, the remainder of the
	 * details are completed here.
	 * 
	 * @param pfRace
	 * @param npc
	 */
	protected void completeDetails(GeneratedNPC npc) {
		PathfinderRaceDefinition pfRace = npc.getPfRace();
		PathfinderClassDefinition pfClass = npc.getPfClass();
		String gender = pfRace.pickGender();
		NameDetails nameDetails = pfRace.pickNameForGender(gender);
		
		npc.setGender(gender);
		npc.setName(nameDetails);
		
		int[] rolls = new int[6];
		int j = 0;	
		
		// Roll up those stats!  
		Dice statDice = new Dice(4, 6);
		for (@SuppressWarnings("unused") EStat stat : EStat.values()) {
			statDice.rollAll();
			int dieRoll = statDice.total(true);
			rolls[j] = dieRoll;
			j++;
		}
		
		// If a class has been picked, assign statistics according to the priority of the statistics as defined for the class.
		if (pfClass != null && pfClass.getStatPriority() != null) {
			log.info("Class selected: " + pfClass.getPathfinderClassName());
			
			// Sort the rolls in ascending order.
			Arrays.sort(rolls);
			
			j = rolls.length - 1;
			// Loop through the class priority for the class, and assign the stats from the most important
			// to the least important.  Note that the "j" counter is incremented above for the population of the
			// array, and then decremented here so that the "rolls" array is accessed from last to first.
			for (EStat stat : pfClass.getStatPriority()) {
				if (stat != null) {
					int adjustment = pfRace.getStatAdjustment(stat.getName());
					int total = rolls[j] + adjustment;
					npc.getStats().put(stat, total);
				}
				j--;
			}					
		} else {
			j = 0;
			
			// If no class is specified, use the order rolls were made
			for (EStat stat : EStat.values()) {
				int adjustment = pfRace.getStatAdjustment(stat.getName());
				int total = rolls[j] + adjustment;
				npc.getStats().put(stat, total);						
				j++;
			}						
		}
		
		@SuppressWarnings("unchecked")
		PercentileList<String> deities = (PercentileList<String>) this.getContextLists().get(DEITY);
		if (deities != null) {
			npc.setDeity(deities.pick());
		}
		
		determineLevelBasedItems(npc);
	}
	
	/**
	 * This is the primary entry point of the logic to generate an NPC.  It assumes that the context of the world
	 * in which the NPC will be generated has already been loaded.
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public GeneratedNPC generateNPC(GenerationConfig gConfig) {
		GeneratedNPC npc = new GeneratedNPC();
		PercentileList<PathfinderRaceDefinition> raceList = (PercentileList<PathfinderRaceDefinition>) contextLists.get(RACE);
		PercentileList<PathfinderClassDefinition> classList = (PercentileList<PathfinderClassDefinition>) contextLists.get(CLASS);
		
		if (raceList != null && classList != null) {
			PathfinderRaceDefinition pfRace = null; 
			PathfinderClassDefinition pfClass = null;
			
			if (gConfig.isClassOverride()) {
				pfClass = classList.findItemByString(gConfig.getFixedClass());
				if (pfClass == null) {
					throw new GameException("Could not set a fixed class of '" + gConfig.getFixedClass() + "'");
				}
			} else {
				pfClass =  classList.pick();
			}
			
			if (gConfig.isRaceOverride()) {
				pfRace = raceList.findItemByString(gConfig.getFixedRace());
				if (pfRace == null) {
					throw new GameException("Could not set a fixed race of '" + gConfig.getFixedRace() + "'");
				}				
			} else {
				pfRace = raceList.pick();
			}			
			
			npc.setPfRace(pfRace);
			npc.setPfClass(pfClass);
			completeDetails(npc);
			
		} else {
			throw new GameException("Incomplete or corrupt definition file - either class or race list was missing.");
		}
		
		return npc;
	}
	
	@Override
	public int compareTo(NpcContext arg0) {
		return this.getName().compareTo(arg0.getName());
	}
	

	
}
