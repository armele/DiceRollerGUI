package com.deathfrog.utils.dice;

import java.util.Random;

/**
 * @author Al Mele
 *
 */
public class Die {
	protected int sides = 0;
	protected int minValue = 1;
	protected int rollResult = -1;
	
	public Die(int sides) {
		this.sides = sides;
	}
	
	public int roll() {
		Random rnd = new Random();
		if (minValue >= sides) {
			rollResult = sides;
		} else {
			rollResult = rnd.nextInt(sides - (minValue - 1)) + minValue;
		}
		return rollResult;
	}
	
	public void show() {
		if (rollResult < 0) {
			roll();
		}
	}
	
	public boolean isRolled() {
		boolean rolled = false;
		if (rollResult > 0) {
			rolled = true;
		}
		return rolled;
	}
	
	public int getRollResult() {
		return rollResult;
	}
	
	public String toString() {
		return new Integer(rollResult).toString();
	}

	/**
	 * @return the minValue
	 */
	public int getMinValue() {
		return minValue;
	}

	/**
	 * @param minValue the minValue to set
	 */
	public void setMinValue(int minValue) {
		this.minValue = minValue;
	}
	
	
}
