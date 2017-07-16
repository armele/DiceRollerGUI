package com.deathfrog.utils;

import java.util.ArrayList;

import com.deathfrog.utils.dice.Die;


public class PercentileList<T> {
	protected ArrayList<T> itemList = new ArrayList<T>();
	protected ArrayList<Integer> chanceList = new ArrayList<Integer>();
	
	/**
	 * Add an object to the random pick list, along with the 
	 * weight for the selection of that object.
	 * 
	 * The weight is relative to the overall weights added.  So
	 * for example, 10 objects added each with a weight of 10 is a total
	 * weight of 100, so each object has a 10% chance of being picked.
	 * 
	 * Null objects are ignored (they won't be added to the pick list, nor 
	 * will their weight be counted in the total).
	 * 
	 * @param item
	 * @param chance
	 */
	public void add(T item, int weight) {
		if (item != null) {
			itemList.add(item);
			
			if (chanceList.size() > 0) {
				int lastChance = chanceList.get(chanceList.size() - 1).intValue();
				weight = weight + lastChance;
				chanceList.add(new Integer(weight));
			} else {
				chanceList.add(new Integer(weight));
			}
		}
	}
	
	/**
	 * @return
	 */
	public T pick() {
		T pickedItem = null;
		
		if (itemList.size() == 0) {
			pickedItem = null;
		} else if (itemList.size() == 1) {
			pickedItem = itemList.get(0);
		} else {
			int max = chanceList.get(chanceList.size() - 1).intValue();
			Die d = new Die(max);
			int selection = d.roll();
			
			for (int i = 0; i < chanceList.size(); i++) {
				if (selection <= chanceList.get(i).intValue()) {
					pickedItem = itemList.get(i);
					break;
				}
			}
		}
		
		return pickedItem;
	}
	
	/**
	 * @return
	 */
	public int totalWeight() {
		int totalWeight = 0;
		
		if (chanceList.size() > 0) {
			totalWeight = chanceList.get(chanceList.size() - 1).intValue();
		}
		
		return totalWeight;
	}
	
	/**
	 * @return
	 */
	public ArrayList<T> getItemList() {
		return itemList;
	}
	
	/**
	 * @return
	 */
	public ArrayList<Integer> getChanceList() {
		return chanceList;
	}

}
