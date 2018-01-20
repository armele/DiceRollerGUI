package com.deathfrog.utils.ui;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;

import com.deathfrog.utils.GameException;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * @author Al Mele
 *
 */
public class InitiativeDisplayGroup implements MouseListener, MouseMoveListener {
	protected static Logger log = LogManager.getLogger(InitiativeDisplayGroup.class);
	public static final int LEFT_BUTTON = 1;
	public static final int RIGHT_BUTTON = 2;

	protected static int UI_STATE_NORMAL = 0;
	protected static int UI_STATE_DRAG = 1;
	
	protected Group uiGroup = null;
	protected InitiativeManager initMgr = null;
	protected ArrayList<ValueLabel> attributes = new ArrayList<ValueLabel>();
	protected Point priorLoc = null;  // Tracked in the Parent coordinate grid (InitiativeManager)
	
	protected int uiState = UI_STATE_NORMAL;
	protected Group dragShadow = null;
	
	/**
	 * @return
	 */
	public Control getControl() {
		return uiGroup;
	}
	
	/**
	 * @return
	 */
	public int getUiState() {
		return uiState;
	}
	
	public InitiativeDisplayGroup(String text, InitiativeManager parent) {
		initMgr = parent;
		uiGroup = new Group(initMgr.getCharacterWindow(), SWT.VERTICAL);
		uiGroup.addMouseListener(this);
		uiGroup.addMouseMoveListener(this);
		uiGroup.setText(text);
		RowLayout cardContentLayout = new RowLayout();
		cardContentLayout.type = SWT.VERTICAL;
        uiGroup.setLayout(cardContentLayout);
     
        /* Prototype Code for value labels... 
        attributes.add(new ValueLabel(uiGroup, "HP", "24"));
        attributes.add(new ValueLabel(uiGroup, "Perception", "5"));
        attributes.add(new ValueLabel(uiGroup, "Stealth", "2"));
        attributes.add(new ValueLabel(uiGroup, "AC", "17"));
        */
        
        // Load attributes for this initiative card from the default file.
		try {
			Scanner attributeFile =  new Scanner(new File("InitiativeAttributes.json"));
			JsonArray ja = readJsonStream(attributeFile);
			for (JsonElement je : ja) {
				log.info(je);
				if(je.isJsonObject()) {
					String attribute = je.getAsJsonObject().get("label").getAsString();
					attributes.add(new ValueLabel(uiGroup, attribute, ""));
				}
			}
		} catch (FileNotFoundException fe) {
			log.error(GameException.fullExceptionInfo(fe));
		} catch (IOException ie) {
			log.error(GameException.fullExceptionInfo(ie));
		}

	}

	/**
	 * @return
	 */
	public String getText() {
		return uiGroup.getText();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
	 */
	@Override
	public void mouseDoubleClick(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events.MouseEvent)
	 */
	@Override
	public void mouseDown(MouseEvent e) {
		
		Device device = Display.getCurrent ();
		
		// A left-mouse click while no drag action is occurring initiates a drag action.
		if (uiState == UI_STATE_NORMAL && e.button == LEFT_BUTTON) {
			log.info("Start Drag: " + uiGroup);
			Color grey = new Color (device, 200, 200, 200);
			uiGroup.setBackground(grey);
			uiState = UI_STATE_DRAG;
			
			Cursor cursor = new Cursor(device, SWT.CURSOR_HAND);
			uiGroup.setCursor(cursor);
			
			priorLoc = Display.getCurrent().map(uiGroup, initMgr.getCharacterWindow(), e.x, e.y);
			log.info ("priorLoc: " + priorLoc);
			
			dragShadow = new Group(initMgr.getCharacterWindow(), uiGroup.getStyle());
			dragShadow.setText(uiGroup.getText());
			dragShadow.setBounds(uiGroup.getBounds());
			uiGroup.setVisible(false);
		}
		
		initMgr.childEventHandler(this, e);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
	 */
	@Override
	public void mouseUp(MouseEvent e) {
		Device device = Display.getCurrent ();
		
		if (uiState == UI_STATE_DRAG && e.button == LEFT_BUTTON) {
			log.info("Stop Drag: " + uiGroup);
			Color green = new Color (device, 0, 255, 0);
			uiGroup.setBackground(green);
			uiState = UI_STATE_NORMAL;
			
			Cursor cursor = new Cursor(device, SWT.CURSOR_ARROW);
			uiGroup.setCursor(cursor);	
			
			if (dragShadow != null) {
				uiGroup.setBounds(dragShadow.getBounds());
				uiGroup.setVisible(true);
				dragShadow.dispose();
				dragShadow = null;
			}
			
			priorLoc = null;
			initMgr.straightenCards();
		}		

		initMgr.childEventHandler(this, e);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events.MouseEvent)
	 */
	@Override
	public void mouseMove(MouseEvent e) {
		
		if (uiState == UI_STATE_DRAG) {
			Device device = Display.getCurrent();
			Color highlight = new Color (device, 25, 25, 25);
			uiGroup.setBackground(highlight);
			
			Point eventPt = Display.getCurrent().map(uiGroup, initMgr.getCharacterWindow(), e.x, e.y);
		    Point offset = new Point (eventPt.x - priorLoc.x, eventPt.y - priorLoc.y);
		    Point newLoc = new Point (dragShadow.getBounds().x + offset.x, dragShadow.getBounds().y + offset.y );
		    dragShadow.setLocation(newLoc);
			priorLoc = eventPt;
			
			
		}
		
		initMgr.childEventHandler(this, e);
	}

	/**
	 * @return
	 */
	public ArrayList<ValueLabel> getAttributes() {
		return attributes;
	}
	
	/**
	 * @param xpos
	 * @param ypos
	 * @param width
	 * @param height
	 */
	public void setBounds(int xpos, int ypos, int width, int height) {
		uiGroup.setBounds(xpos, ypos, width, height);
	}

	
	/**
	 * @param source
	 * @param childEvent
	 */
	public void siblingEventHandler(InitiativeDisplayGroup source, MouseEvent childEvent) {
		// log.info("Sibling Event from: " + source + " for " + uiGroup + ": " + childEvent);
	}
	
	/**
	 * Adjust the contents of the card for the scale they're being shown at, and
	 * mark items on the card for layout refresh.
	 */
	public void requestLayout(double scale) {	
		for (ValueLabel vl : attributes) {
			vl.positionControls(scale);
		}
		
		uiGroup.requestLayout();
	}
	
	/**
	 * @param in
	 * @return
	 * @throws IOException
	 */
	protected JsonArray readJsonStream(Scanner in) throws IOException {
		StringBuilder sb = new StringBuilder();
		while(in.hasNext()) {
			sb.append(in.next());
		}
		in.close();
		
		JsonParser parser = new JsonParser();
		JsonElement element = parser.parse(sb.toString());
	    JsonObject  jobject = element.getAsJsonObject();
	    JsonArray jarray = jobject.getAsJsonArray("attributes");
	    
        return jarray;
    }

}
