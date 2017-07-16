package com.deathfrog.npc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import com.deathfrog.utils.PercentileList;

public class TestPercentileList {

	protected Logger logger = LogManager.getLogger(TestPercentileList.class);
	
	@Test
	public void test() {
		PercentileList<String> pl = new PercentileList<String>();
		
		pl.add("Blue", 10);
		pl.add("Gold", 10);
		pl.add("Red", 10);
		pl.add("White", 10);
		pl.add("Black", 10);
		pl.add("Brown", 10);
		pl.add("Silver", 10);
		pl.add("Purple", 10);
		pl.add("Orange", 10);
		pl.add("Peach", 10);
		pl.add("Yellow", 10);
		
		
		for (int i = 0; i < 24; i++) {
			String termannic = pl.pick();
			logger.info(termannic);
		}
	}

}
