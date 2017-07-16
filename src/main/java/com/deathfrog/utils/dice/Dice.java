package com.deathfrog.utils.dice;

import java.util.ArrayList;

/**
 * A "bag" of dice, implemented as an array of Die objects.
 * @author Al Mele
 *
 */
public class Dice extends ArrayList<Die> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2141473157895666412L;

	/**
	 * 
	 */
	public Dice() {
		
	}
	
	/**
	 * @param numDice
	 * @param sides
	 */
	public Dice(int numDice, int sides) {
		init(numDice, sides);
	}
	
	/**
	 * @param numDice
	 * @param sides
	 */
	public void init(int numDice, int sides) {
		this.clear();
		for (int i = 0; i < numDice ; i++) {
			Die d = new Die(sides);
			this.add(d);
		}
	}
	
	/**
	 * 
	 */
	public void rollAll() {
		for (Die d : this) {
			d.roll();
		}
	}
	
	/**
	 * 
	 */
	public void show() {
		for (Die d : this) {
			d.show();
			System.out.print(", ");
		}
	}
	
	/**
	 * @return
	 */
	public int total(boolean dropLowest) {
		int total = 0;
		int lowest = Integer.MAX_VALUE;
		
		for (Die d : this) {
			if (!d.isRolled()){
				d.roll();
			}
			int result = d.getRollResult();
			
			if (lowest > result) {
				lowest = result;
			}
			
			total = total + result;
		}	
		
		if (total > 0 && dropLowest) {
			total = total - lowest;
		}
		
		return total;
	}
	
	/* (non-Javadoc)
	 * @see java.util.AbstractCollection#toString()
	 */
	public String toString() {
		StringBuffer buf = new StringBuffer();
		int i = 0;
		
		for (Die d : this) {
			i++;
			buf.append(d.toString());
			if (i < this.size()) {
				buf.append(", ");
			}
		}	
		
		return buf.toString();
	}
}
