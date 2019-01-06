package com.deathfrog.npc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.deathfrog.utils.PercentileList;

public class TestNPC {
	protected Logger logger = LogManager.getLogger(TestNPC.class);
	
	protected static NpcDefinitions npcDef = new NpcDefinitions();
	
	@BeforeClass
	public static void readXml() {
		NpcPersistor npc = new NpcPersistor();
		npcDef = npc.parse(null);		
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testFileLoad() {		
		Assert.assertEquals(3, npcDef.size()); 
		NpcContext context = npcDef.get(0);
		Assert.assertEquals("Sigil", context.getName());
		Assert.assertEquals(3, context.getContextLists().size());
		
		PercentileList<PathfinderClassDefinition> clist = (PercentileList<PathfinderClassDefinition>) context.getContextLists().get("Class");
		Assert.assertEquals(320, clist.totalWeight());
		
		for (PathfinderClassDefinition pc : clist.getItemList()) {
			logger.info(pc.getPathfinderClassName());
			
			if ("Barbarian".equals(pc.getPathfinderClassName())) {
				Assert.assertEquals(EStat.STR, pc.getStatPriority(0));
				Assert.assertEquals(EStat.CON, pc.getStatPriority(1));
				Assert.assertEquals(EStat.DEX, pc.getStatPriority(2));
				Assert.assertEquals(EStat.CHA, pc.getStatPriority(3));
				Assert.assertEquals(EStat.WIS, pc.getStatPriority(4));
				Assert.assertEquals(EStat.INT, pc.getStatPriority(5));
				
				Assert.assertEquals(12, pc.getHitdice());
				
				for (int i = 1; i <= 20; i++) {
					Assert.assertEquals(i, pc.getBabForLevel(i));
				}
			}
		}
		
		PercentileList<PathfinderRaceDefinition> rlist = (PercentileList<PathfinderRaceDefinition>) context.getContextLists().get("Race");
		Assert.assertEquals(179, rlist.totalWeight());
		
		for (PathfinderRaceDefinition pc : rlist.getItemList()) {
			logger.info(pc.getRace());
		}		
		
		NameGenerator gen = NpcDefinitions.getNameGen("default");
		
		Assert.assertEquals(5, gen.getPatternList().length);
		Assert.assertNotNull(gen.getBlendList());
		Assert.assertNotNull(gen.getConsonentList());
		Assert.assertNotNull(gen.getVowelList());
		Assert.assertNotNull(gen.getPatternList());
		
		PathfinderRaceDefinition dwarf = context.getRace("Dwarf");
		
		Assert.assertEquals(40, dwarf.getMinage());
		Assert.assertEquals(450, dwarf.getMaxage());
		
	}
	
	@Test
	public void testGeneration() {
		Assert.assertEquals(3, npcDef.size()); 
		
		for (NpcContext context : npcDef) {
			PathfinderRaceDefinition dwarf = context.getRace("Dwarf");
			String gender = dwarf.pickGender();
			
			Assert.assertNotNull(gender);
			
			logger.info(gender);
			
			int age = dwarf.pickAge();
			
			Assert.assertTrue(age > 0);
			
			for (EStat stat : EStat.values()) {
				int bonus = dwarf.getStatAdjustment(stat.getName());
				
				switch (stat) {
				case STR:
					Assert.assertEquals(0, bonus);
					break;
				case DEX:
					Assert.assertEquals(0, bonus);
					break;
				case CON:
					Assert.assertEquals(2, bonus);
					break;
				case INT:
					Assert.assertEquals(0, bonus);
					break;
				case WIS:
					Assert.assertEquals(2, bonus);
					break;
				case CHA:
					Assert.assertEquals(-2, bonus);
					break;					
				}
			}
			
			logger.info(age);
			
			NameDetails name = dwarf.pickNameForGender(gender);
			
			Assert.assertNotNull(name);
			
			logger.info(name);
		}
	}

}
