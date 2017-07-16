package com.deathfrog.npc;

import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.widgets.Text;

import com.deathfrog.utils.PercentileList;
import com.deathfrog.utils.dice.Dice;

/**
 * Saves the name of the context as well as any associated percentile lists for that context.
 * 
 * @author Al Mele
 *
 */
public class NpcContext implements Comparable<NpcContext> {
	protected static Logger log = LogManager.getLogger(NpcContext.class);
	public static final String RACE = "Race";
	public static final String CLASS = "Class";
	
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
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String pickEverything() {
		StringBuffer sb = new StringBuffer();
		PercentileList<PathfinderRaceDefinition> raceList = (PercentileList<PathfinderRaceDefinition>) contextLists.get(RACE);
		PercentileList<PathfinderClassDefinition> classList = (PercentileList<PathfinderClassDefinition>) contextLists.get(CLASS);
		
		if (raceList != null && classList != null) {
			PathfinderRaceDefinition pfRace = raceList.pick();
			PathfinderClassDefinition pfClass = classList.pick();
			
			if (pfRace != null) {
				String gender = pfRace.pickGender();
				String name = pfRace.pickNameForGender(gender);
				sb.append(name).append(", ").append(pfRace.pickAge()).append(" year old ").append(gender).append(" ");
				sb.append(pfRace.getRace()).append(" ");
				
				if (pfClass != null) {
					sb.append(pfClass.getPathfinderClassName() + " ");
				} else {
					sb.append("Undefined class.");
				}		
				
				sb.append(Text.DELIMITER);
				
				Dice statDice = new Dice(4, 6);
				for (EStat stat : EStat.values()) {
					statDice.rollAll();
					int dieRoll = statDice.total(true);
					int adjustment = pfRace.getStatAdjustment(stat.getName());
					int total = dieRoll + adjustment;
					sb.append(stat.getName().toUpperCase() + ": " + dieRoll + " [" + adjustment + "] = " + total + Text.DELIMITER);
				}
			} else {
				sb.append("Undefined race. ");

				
				if (pfClass != null) {
					sb.append(pfClass.getPathfinderClassName() + " ");
				} else {
					sb.append("Undefined class.");
				}						
			}
			
		} else {
			sb.append("Incomplete definition file loaded.  (Cannot identify races or classes.)");
		}
		
		return sb.toString();
	}
	
	@Override
	public int compareTo(NpcContext arg0) {
		return this.getName().compareTo(arg0.getName());
	}
	

	
}
