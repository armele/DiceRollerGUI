package com.deathfrog.npc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

public class TestNaming {
	protected Logger logger = LogManager.getLogger(TestNaming.class);
	
	@Test
	public void testNaming() {
		NameGenerator ng = new NameGenerator();
		
		String name = ng.generate();
		logger.info(name);
		
		name = ng.generate();
		logger.info(name);

		name = ng.generate();
		logger.info(name);

	}
	
	@Test
	public void testDoublingLogic() {
		NameGenerator ng = new NameGenerator();
		ng.setBlendList(new String[]{"1"});
		ng.setConsonentList(new String[]{"2"});
		ng.setVowelList(new String[]{"3"});
		ng.setPatternList(new String[]{"c(b)v"});
		
		String name = ng.generate();
		logger.info(name);
		
		Assert.assertEquals("2113", name);
		
		
		ng.setPatternList(new String[]{"c(vbv)c"});
		
		name = ng.generate();
		logger.info(name);
		
		Assert.assertEquals("23133132", name);		
		
		ng.setPatternList(new String[]{"(cvc)"});
		
		name = ng.generate();
		logger.info(name);
		
		Assert.assertEquals("232232", name);		

	}
	
	@Test
	public void testFirstUpper() {
		NameGenerator ng = new NameGenerator();
		String testval = "n";
		String testresult = ng.firstUpper(testval);
		
		Assert.assertEquals("N", testresult);
		
		testval = "no";
		testresult = ng.firstUpper(testval);
		
		Assert.assertEquals("No", testresult);		
		
		testval = "noodle";
		testresult = ng.firstUpper(testval);
		
		Assert.assertEquals("Noodle", testresult);
		
		testval = "NOCTURNAL";
		testresult = ng.firstUpper(testval);
		
		Assert.assertEquals("Nocturnal", testresult);		
	}

}
