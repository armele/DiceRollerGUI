package com.deathfrog.npc;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.deathfrog.utils.GameException;
import com.deathfrog.utils.PercentileList;

public class NpcPersistor {
	protected static Logger log = LogManager.getLogger(NpcPersistor.class);
	
	public final String XML_CONTEXT = "context";
	public final String XML_NAME = "name";
	public final String XML_PICKLIST = "picklist";
	public final String XML_CHOICE = "choice";
	public final String XML_CHANCE = "chance";
	public final String XML_NAMEGENERATOR = "namegenerator";
	public final String XML_PATTERNLIST = "patternList";
	public final String XML_CONSONENTLIST = "consonentList";
	public final String XML_VOWELLIST = "vowelList";
	public final String XML_BLENDLIST = "blendList";
	public final String XML_GENDER = "gender";
	public final String XML_AGE = "age";
	public final String XML_MIN = "min";
	public final String XML_MAX = "max";
	public final String XML_BAB = "bab";
	public final String XML_STATADUSTMENT = "statadjustment";
	public final String XML_HITDICE = "hitdice";
	
	protected static String defaultDefinitionFile = "com/deathfrog/utils/definitions.xml";
	
	/**
	 * @param fileLoc
	 * @return
	 * @throws GameException
	 */
	public NpcDefinitions parse(String filename) throws GameException {
		NpcDefinitions npcdef = new NpcDefinitions();
		Document doc = null;
		
		if (filename == null) {
			
			log.info("Loading default NPC definition file.");
			InputStream defConfig = NpcPersistor.class.getClassLoader().getResourceAsStream(defaultDefinitionFile);

			if (defConfig != null) {
				doc = loadDocument(defConfig);
			} else {
				throw new GameException("Could not load default NPC definition file - not found as a resource: " + defaultDefinitionFile);
			}
		} else {
			log.info("Loading NPC definition file: " + filename);
			try {
				doc = loadDocument(filename);
			} catch (FileNotFoundException e) {
				throw new GameException("Could not load NPC definition file: " + filename, e);
			}
		}
		
		if (doc != null) {
			loadGlobals(doc, npcdef);
			loadContext(doc, npcdef);
		} else {
			throw new GameException("Could not load document: " + filename);
		}
		log.info("Document loaded.");
		
		return npcdef;
	}	
	
	/**
	 * Load anything that is not context-specific:
	 * 		Name Generator Configurations
	 * 
	 * @param doc
	 * @param npcdef
	 */
	protected void loadGlobals(Document doc, NpcDefinitions npcdef) {
		log.info("Loading globals.");
		NodeList docChildren = doc.getFirstChild().getChildNodes();
		
		if (docChildren != null) {
			for (int i = 0; i < docChildren.getLength(); i++) {
				Node n = docChildren.item(i);
				
				if (XML_NAMEGENERATOR.equalsIgnoreCase(n.getNodeName())) {
					NameGenerator generator = new NameGenerator();
					String name = n.getAttributes().getNamedItem(XML_NAME).getNodeValue();
					NpcDefinitions.addNameGen(name, generator);
					
					NodeList ngDefs = n.getChildNodes();
					
					if (ngDefs != null) {
						for (int j = 0; j < ngDefs.getLength(); j++) {
							Node compNode = ngDefs.item(j);
							
							if (Node.ELEMENT_NODE == compNode.getNodeType()) {
								String component = compNode.getNodeName();
								String[] list = compNode.getFirstChild().getNodeValue().split(",");
								
								if (XML_PATTERNLIST.equalsIgnoreCase(component)) {
									generator.setPatternList(list);
								} else if (XML_CONSONENTLIST.equalsIgnoreCase(component)) {
									generator.setConsonentList(list);
								} else if (XML_VOWELLIST.equalsIgnoreCase(component)) {
									generator.setVowelList(list);
								} else if (XML_BLENDLIST.equalsIgnoreCase(component)) {
									generator.setBlendList(list);
								}	
							}
						}
					}
					
					log.info("Loaded global namegenerator: " + name);
				// Races can be defined globally or locally to the context.  This loads the global definition.
				} else if (NpcContext.RACE.equalsIgnoreCase(n.getNodeName())) {
					PathfinderRaceDefinition pfRace = loadRace(n);
					NpcDefinitions.addRace(pfRace.getRace(), pfRace);
					log.info("Loaded global race: " + pfRace.getRace());
				} else if (NpcContext.CLASS.equalsIgnoreCase(n.getNodeName())) {
					PathfinderClassDefinition pfClass = loadClass(n);	
					NpcDefinitions.addClass(pfClass.getPathfinderClassName(), pfClass);
					log.info("Loaded global class: " + pfClass.getPathfinderClassName());
				}
			}
		}		
	}
	
	/**
	 * @param file
	 * @return
	 */
	protected Document loadDocument(InputStream file) {
		Document doc = null;
		
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.parse(file);
			doc.getDocumentElement().normalize();
		} catch (ParserConfigurationException pex) {
			throw new GameException("Error constructing document builder " + file, pex);
		} catch (SAXException sex) {
			throw new GameException("SAX Error parsing XML file " + file, sex);
		} catch (IOException iox) {
			throw new GameException("IO Error parsing XML file " + file, iox);
		}
		
		return doc;
	}
	
	/**
	 * @param fileLoc
	 * @return
	 * @throws FileNotFoundException 
	 */
	protected Document loadDocument(String filename) throws FileNotFoundException {
		Document doc = null;
		FileInputStream file = null;
		
		if (filename != null) {
			file = new FileInputStream(filename);
			
			doc = loadDocument(file);
		}
		
		return doc;
	}
	
	/**
	 * Given an XML document, find all contexts defined within it and load them
	 * into the NPC Definitions object for use.
	 * 
	 * @param doc
	 * @param npcdef
	 */
	protected void loadContext(Document doc, NpcDefinitions npcdef) {
		NodeList contextList = doc.getElementsByTagName(XML_CONTEXT);
		
		if (contextList != null) {
			for (int i = 0; i < contextList.getLength(); i++) {
				Node n = contextList.item(i);
				NpcContext context = new NpcContext();
				String contextName = n.getAttributes().getNamedItem(XML_NAME).getNodeValue();
						
				log.info("Loading context " + contextName);
						
				context.setName(contextName);
				npcdef.add(context);
				
				loadRaceAndClass(context, n);
				loadPicklists(context, n);
			}
		}
	}
	
	
	/**
	 * Set up the PathfinderRaceDefinition object from the XML configuration for it.
	 * 
	 * @param context
	 * @param raceNode
	 */
	protected PathfinderRaceDefinition loadRace(Node raceNode) {
		PathfinderRaceDefinition pfRace = 
				new PathfinderRaceDefinition(raceNode.getAttributes().getNamedItem(XML_NAME).getNodeValue());
		
		NodeList raceChildren = raceNode.getChildNodes();
		
		for (int i = 0 ; i < raceChildren.getLength(); i++) {
			Node n = raceChildren.item(i);
			
			if (XML_GENDER.equalsIgnoreCase(n.getNodeName())) {
				String gender = n.getFirstChild().getNodeValue();
				String nameGen = n.getAttributes().getNamedItem(XML_NAMEGENERATOR).getNodeValue();
				Integer chance = new Integer(n.getAttributes().getNamedItem(XML_CHANCE).getNodeValue());
				
				pfRace.addGenderOption(gender, chance.intValue(), nameGen);
				
			} else if (XML_AGE.equalsIgnoreCase(n.getNodeName())) {
				Integer min = new Integer(n.getAttributes().getNamedItem(XML_MIN).getNodeValue());
				Integer max = new Integer(n.getAttributes().getNamedItem(XML_MAX).getNodeValue());
				
				pfRace.setMaxage(max.intValue());
				pfRace.setMinage(min.intValue());
			} else if (XML_STATADUSTMENT.equalsIgnoreCase(n.getNodeName())) {
				NodeList children = n.getChildNodes();
				
				for (int j = 0; j < children.getLength(); j++) {
					Node statblock = children.item(j);
					String stat = statblock.getNodeName();
					
					if (EStat.validStat(stat)) {
						String statValue = statblock.getFirstChild().getNodeValue();
						Integer value = null;
						
						if (statValue != null && !statValue.trim().isEmpty()) {
							value = new Integer(statValue);
						} else {
							value = 0;
						}
						
						pfRace.addStatAdjustment(stat, value);
					}
				}
			}
		}
		
		return pfRace;	
	}
	
	/**
	 * @param context
	 * @param classNode
	 */
	protected PathfinderClassDefinition loadClass(Node classNode) {
		PathfinderClassDefinition pfClass = 
				new PathfinderClassDefinition(classNode.getAttributes().getNamedItem(XML_NAME).getNodeValue());
		
		NodeList contextChildren = classNode.getChildNodes();
		
		for (int i = 0 ; i < contextChildren.getLength(); i++) {
			Node n = contextChildren.item(i);
			
			if (NpcContext.STATPRIORITY.equalsIgnoreCase(n.getNodeName())) {
				String[] statOrder = n.getFirstChild().getNodeValue().split(",");
				int j = 0;
				for (String stat : statOrder) {
					pfClass.setStatPriority(j, EStat.statForString(stat));
					j++;
				}
			} else if (XML_BAB.equalsIgnoreCase(n.getNodeName())) {
				String babProgression = n.getFirstChild().getNodeValue();
				String babArray[] = babProgression.split(",");
				int level = 1;
				for (String bab : babArray) {
					pfClass.setBabForLevel(level, new Integer(bab));
					level++;
				}
			} else if (XML_HITDICE.equalsIgnoreCase(n.getNodeName())) {
				pfClass.setHitdice(new Integer(n.getFirstChild().getNodeValue()));
			}
		}
		
		return pfClass;			
	}
	
	/**
	 * @param context
	 * @param contextNode
	 */
	protected void loadRaceAndClass(NpcContext context, Node contextNode) {		
		NodeList contextChildren = contextNode.getChildNodes();
		
		for (int i = 0 ; i < contextChildren.getLength(); i++) {
			Node n = contextChildren.item(i);
			
			if (NpcContext.RACE.equalsIgnoreCase(n.getNodeName())) {
				PathfinderRaceDefinition pfRace = loadRace(n);
				context.addRace(pfRace.getRace(), pfRace);	
			} else if (NpcContext.CLASS.equalsIgnoreCase(n.getNodeName())) {
				PathfinderClassDefinition pfClass = loadClass(n);
				context.addClass(pfClass.getPathfinderClassName(), pfClass);
			}
		}
	}
	
	/**
	 * @param context
	 * @param choices
	 * @param picklistName
	 */
	protected void loadRacePicklist(NpcContext context, NodeList choices, String picklistName) {
		PercentileList<PathfinderRaceDefinition> picklist = new PercentileList<PathfinderRaceDefinition>();
		
		for (int j = 0; j < choices.getLength(); j++) {
			Node choice = choices.item(j);
			
			if (choice.hasAttributes()) {	
				String choicename = choice.getAttributes().getNamedItem(XML_NAME).getNodeValue();
				Integer chance = new Integer(choice.getAttributes().getNamedItem(XML_CHANCE).getNodeValue());

				PathfinderRaceDefinition pfRace = context.getRace(choicename);
				
				if (pfRace != null) {
					picklist.add(pfRace, chance);
				} else {
					log.error("No race definitions found for " + choicename);
				}
			}
		}
		
		context.addContextList(picklistName, picklist);
	}	
	
	/**
	 * @param context
	 * @param choices
	 * @param picklistName
	 */
	protected void loadClassPicklist(NpcContext context, NodeList choices, String picklistName) {
		PercentileList<PathfinderClassDefinition> picklist = new PercentileList<PathfinderClassDefinition>();
		
		for (int j = 0; j < choices.getLength(); j++) {
			Node choice = choices.item(j);
			
			if (choice.hasAttributes()) {			
				String choicename = choice.getAttributes().getNamedItem(XML_NAME).getNodeValue();
				Integer chance = new Integer(choice.getAttributes().getNamedItem(XML_CHANCE).getNodeValue());
			
				PathfinderClassDefinition pfClass = context.getPathfinderClass(choicename);
				
				if (pfClass != null) {
					picklist.add(pfClass, chance);
				} else {
					log.error("No class definitions found for [" + choicename + "] - using a 'name only' configuration.");
					pfClass = new PathfinderClassDefinition(choicename);
					picklist.add(pfClass,  chance);
				}
			}
		}

		context.addContextList(picklistName, picklist);		
	}
	
	
	/**
	 * @param context
	 * @param choices
	 * @param picklistName
	 */
	protected void loadOtherPicklist(NpcContext context, NodeList choices, String picklistName) {
		PercentileList<String> picklist = new PercentileList<String>();
		
		for (int j = 0; j < choices.getLength(); j++) {
			Node choice = choices.item(j);
			
			if (choice.hasAttributes()) {			
				String choicename = choice.getAttributes().getNamedItem(XML_NAME).getNodeValue();
				Integer chance = new Integer(choice.getAttributes().getNamedItem(XML_CHANCE).getNodeValue());
				picklist.add(choicename, chance);
			}
		}

		context.addContextList(picklistName, picklist);		
	}
	
	/**
	 * Given a context object and the XML node that represents it, load the
	 * child picklist definitons.  This relies on the classes and races
	 * referenced in the lists having already been loaded.
	 * 
	 * @param context
	 * @param node
	 */
	protected void loadPicklists(NpcContext context, Node node) {
		node.normalize();
		NodeList contextChildren = node.getChildNodes();
				
		// Examine all the children of the context to find the picklists, and load their choices.
		if (contextChildren != null) {
			for (int i = 0; i < contextChildren.getLength(); i++) {
				Node childNode = contextChildren.item(i);
				
				if (XML_PICKLIST.equalsIgnoreCase(childNode.getNodeName())) {
					String picklistName = childNode.getAttributes().getNamedItem(XML_NAME).getNodeValue();
					NodeList choices = childNode.getChildNodes();
					
					// If this is a list of classes, load them as classes
					if (NpcContext.CLASS.equalsIgnoreCase(picklistName)) {
						loadClassPicklist(context, choices, picklistName);
					// If this is a list of races, load them as races.
					} else if (NpcContext.RACE.equalsIgnoreCase(picklistName)) {
						loadRacePicklist(context, choices, picklistName);
					// Otherwise, load them as strings.
					} else {
						loadOtherPicklist(context, choices, picklistName);						
					}
				}
			}
		}		
	}

}
