package com.deathfrog.npc;

import org.junit.Test;

import junit.framework.Assert;

public class TestStat {

	@Test
	public void test() {
		Assert.assertEquals(-5, EStat.bonus(1));
		Assert.assertEquals(-4, EStat.bonus(2));
		Assert.assertEquals(-4, EStat.bonus(3));
		Assert.assertEquals(-3, EStat.bonus(4));
		Assert.assertEquals(-3, EStat.bonus(5));
		Assert.assertEquals(-2, EStat.bonus(6));
		Assert.assertEquals(-2, EStat.bonus(7));
		Assert.assertEquals(-1, EStat.bonus(8));
		Assert.assertEquals(-1, EStat.bonus(9));
		Assert.assertEquals(-0, EStat.bonus(10));
		Assert.assertEquals(-0, EStat.bonus(11));
		Assert.assertEquals( 1, EStat.bonus(12));
		Assert.assertEquals( 1, EStat.bonus(13));
		Assert.assertEquals( 2, EStat.bonus(14));
		Assert.assertEquals( 2, EStat.bonus(15));
		Assert.assertEquals( 3, EStat.bonus(16));
		Assert.assertEquals( 3, EStat.bonus(17));
		Assert.assertEquals( 4, EStat.bonus(18));
		Assert.assertEquals( 4, EStat.bonus(19));
		Assert.assertEquals( 5, EStat.bonus(20));
		Assert.assertEquals( 5, EStat.bonus(21));
		Assert.assertEquals( 6, EStat.bonus(22));
		Assert.assertEquals( 6, EStat.bonus(23));
		Assert.assertEquals( 7, EStat.bonus(24));
	}

}
