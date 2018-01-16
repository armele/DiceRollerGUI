package com.deathfrog.utils.ui;


import java.util.ArrayList;

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
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;

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
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        uiGroup.setLayout(gridLayout);
     
        /* Prototype Code for value labels... */
        attributes.add(new ValueLabel(uiGroup, "HP", "24"));
        attributes.add(new ValueLabel(uiGroup, "Perception", "5"));
        attributes.add(new ValueLabel(uiGroup, "Stealth", "2"));
        attributes.add(new ValueLabel(uiGroup, "AC", "17"));
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
	 * Mark all children of the Composite control for re-layout, and
	 * all descendants of those children.
	 * 
	 * @param parent
	 */
	protected void requestChildLayout(Composite parent) {
		for (Control c : parent.getChildren()) {
			if (c instanceof Composite) {
				requestChildLayout((Composite)c);
			}
		}
	}
	
	/**
	 * Mark items on the card for layout refresh.
	 */
	public void requestLayout() {	
		uiGroup.requestLayout();
		requestChildLayout(uiGroup);
	}
}
