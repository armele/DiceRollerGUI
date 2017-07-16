package com.deathfrog.npc;

import java.util.HashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.deathfrog.utils.GameException;
import com.deathfrog.utils.PercentileList;
import com.deathfrog.utils.dice.Die;


/**
 * Because names are racially variant, the name is associated with the Race object.
 * 
 * @author Al Mele
 *
 */
public class PathfinderRaceDefinition {
	protected static Logger log = LogManager.getLogger(PathfinderRaceDefinition.class);
	
	protected String race;
	protected int minage;
	protected int maxage;
	protected PercentileList<String> genderOptions = new PercentileList<String>(); 
	protected HashMap<String, String> genderNames = new HashMap<String, String>(); // A map of gender names and which name generators they use.	
	protected HashMap<String, Integer> statAdjustments = new HashMap<String, Integer>();  
	
	public PathfinderRaceDefinition(String raceName) {
		race = raceName;
	}
	
	/**
	 * @return the name
	 */
	public String getRace() {
		return race;
	}

	/**
	 * @param option
	 * @param chance
	 * @param namegen
	 */
	public void addGenderOption(String option, int chance, String generatorName) {
		genderOptions.add(option, chance);
		genderNames.put(option, generatorName);
	}
	
	/**
	 * @return
	 */
	public String pickGender() {
		return genderOptions.pick();
	}
	
	/**
	 * Randomly select an age in the defined age range.
	 * 
	 * @return
	 */
	public int pickAge() {
		Die d = new Die(maxage);
		d.setMinValue(minage);
		return d.roll();
	}
	
	
	/**
	 * @param gender
	 * @return
	 */
	public String pickNameForGender(String gender) {
		String generatorName = genderNames.get(gender);
		NameGenerator nameGen = NpcDefinitions.getNameGen(generatorName);
		String name;
		
		if (nameGen != null) {
			name = nameGen.generate();
		} else {
			throw new GameException("No name found for name generator " + generatorName + " from gender " + gender);
		}
		
		return name;
	}

	/**
	 * @return the minage
	 */
	public int getMinage() {
		return minage;
	}

	/**
	 * @param minage the minage to set
	 */
	public void setMinage(int minage) {
		this.minage = minage;
	}

	/**
	 * @return the maxage
	 */
	public int getMaxage() {
		return maxage;
	}

	/**
	 * @param maxage the maxage to set
	 */
	public void setMaxage(int maxage) {
		this.maxage = maxage;
	}
	
	/**
	 * @param stat
	 * @param adjustmentValue
	 */
	public void addStatAdjustment(String stat, Integer adjustmentValue) {
		statAdjustments.put(stat, adjustmentValue);
	}
	
	/**
	 * @param stat
	 * @return
	 */
	public Integer getStatAdjustment(String stat) {
		return statAdjustments.get(stat);
	}
	
}	
