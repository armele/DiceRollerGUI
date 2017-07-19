package com.deathfrog.npc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.deathfrog.utils.dice.Die;

/**
 * See http://www.alt-codes.net/ for use of "odd" characters.
 * @author Al Mele
 *
 */
/**
 * @author Al Mele
 *
 */
public class NameGenerator {
	protected static Logger log = LogManager.getLogger(NameGenerator.class);
	Integer minLength;
	Integer maxLength;
	
	protected String[] vowelList = {"a", "e", "i", "o", "u"};
	protected String[] consonentList = {"b", "c", "d", "f", "g", "h", "j", "k", "l", "m", "n", "p", "q", "r", "s", "t", "v", "w", "x", "y", "z"};
	protected String[] blendList = {"l", "s", "r"};
	protected String[] patternList = {"Cvccvc", "Cvccvbc", "Cbvcvvc", "Vbccvbc"};
	
	/**
	 * 
	 */
	public NameGenerator() {	
	}
	
	/**
	 * @return the vowelList
	 */
	public String[] getVowelList() {
		return vowelList;
	}

	/**
	 * @param vowelList the vowelList to set
	 */
	public void setVowelList(String[] vowelList) {
		this.vowelList = vowelList;
	}

	/**
	 * @return the consonentList
	 */
	public String[] getConsonentList() {
		return consonentList;
	}

	/**
	 * @param consonentList the consonentList to set
	 */
	public void setConsonentList(String[] consonentList) {
		this.consonentList = consonentList;
	}

	/**
	 * @return the blendList
	 */
	public String[] getBlendList() {
		return blendList;
	}

	/**
	 * @param blendList the blendList to set
	 */
	public void setBlendList(String[] blendList) {
		this.blendList = blendList;
	}

	/**
	 * @return the patternList
	 */
	public String[] getPatternList() {
		return patternList;
	}

	/**
	 * @param patternList the patternList to set
	 */
	public void setPatternList(String[] patternList) {
		this.patternList = patternList;
	}

	/**
	 * Takes the string "text" and returns it
	 * in a format such that the first letter is capitalized
	 * and any others are lower case.
	 * 
	 * @param text
	 * @return
	 */
	protected String firstUpper(String text) {
		String fixed = null;
		
		if (text != null) {
			if (text.length() == 1) {
				fixed = text.toUpperCase();
			} else {
				String tempFirst = text.substring(0, 1);
				String tempLast = text.substring(1);
				fixed = tempFirst.toUpperCase() + tempLast.toLowerCase();
			}
		}
		
		return fixed;
	}
	
	/**
	 * @param npcClass
	 * @return
	 */
	public NameDetails generate() {
		NameDetails name = new NameDetails();
		boolean repeatSegment = false;
		StringBuffer result = new StringBuffer();
		StringBuffer repeat = new StringBuffer();
		
		Die d = new Die(patternList.length);
		String pattern = patternList[d.roll() - 1];
		
		log.debug("Using pattern: " + pattern);
		
		for (int i = 0; i < pattern.length(); i++) {
			Character patternVal = pattern.charAt(i);
			String temp = null;
			
			switch (patternVal) {
				case 'v':
					d = new Die(vowelList.length);
					temp = vowelList[d.roll() - 1];
					break;
			
				case 'c':
					d = new Die(consonentList.length);
					temp = consonentList[d.roll() - 1];
					break;
				
				case 'b':
					d = new Die(blendList.length);
					temp = blendList[d.roll() - 1];
					break;
					
				case 'V':
					d = new Die(vowelList.length);
					temp = firstUpper(vowelList[d.roll() - 1]);
					break;
			
				case 'C':
					d = new Die(consonentList.length);
					temp = firstUpper(consonentList[d.roll() - 1]);
					break;
				
				case 'B':
					d = new Die(blendList.length);
					temp = firstUpper(blendList[d.roll() - 1]);
					break;				
					
				case '(':
					repeatSegment = true;
					temp = null;
					break;
				
				case ')':
					repeatSegment = false;
					temp = repeat.toString() + repeat.toString();
					repeat = new StringBuffer();
					break;
					
				default:
					temp = patternVal.toString();
					break;
					
			}
			
			if (temp != null) {
				if (repeatSegment) {
					repeat.append(temp);
				} else {
					result.append(temp);
				}
			}
			
		}
		
		name.setName(result.toString());
		name.setPattern(pattern);
		
		return name;
	}
}
