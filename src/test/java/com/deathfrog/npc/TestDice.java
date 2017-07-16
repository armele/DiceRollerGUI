package com.deathfrog.npc;

import org.junit.Test;

import com.deathfrog.utils.dice.Dice;
import com.deathfrog.utils.dice.Die;

import junit.framework.Assert;

public class TestDice {

	@Test
	public void testMinValue() {
		Die d = new Die(6);
		d.setMinValue(6);
		d.roll();
		
		Assert.assertEquals(6, d.getRollResult());
	}
	
	@Test
	public void testDiceBag() {
		Dice dice = new Dice();
		Die d1 = new Die(2);
		Die d2 = new Die(6);
		d2.setMinValue(3);
		Die d3 = new Die(6);
		d3.setMinValue(3);
		Die d4 = new Die(6);
		d4.setMinValue(3);		
		
		dice.add(d1);
		dice.add(d2);
		dice.add(d3);
		dice.add(d4);
		
		dice.rollAll();
		
		int lowest = d1.getRollResult();
		int total = dice.total(false);
		int subtotal = d2.getRollResult() + d3.getRollResult() + d4.getRollResult();
		int droppedtotal = dice.total(true);
		
		Assert.assertEquals(subtotal, droppedtotal);
		Assert.assertEquals(lowest, total - subtotal);
	}	

}
