package com.deathfrog.ui.initiative;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;

import com.deathfrog.utils.GameException;
import com.deathfrog.utils.JsonUtils;
import com.deathfrog.utils.ui.LaunchPad;
import com.deathfrog.utils.ui.SWTResourceManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.annotations.Expose;

/**
 * @author Al Mele
 *
 */
public class InitiativeDisplayGroup implements MouseListener, MouseMoveListener {
	protected static Logger log = LogManager.getLogger(InitiativeDisplayGroup.class);
	public static final int LEFT_BUTTON = 1;
	public static final int RIGHT_BUTTON = 3;

	protected static int UI_STATE_NORMAL = 0;
	protected static int UI_STATE_DRAG = 1;
	protected static int UI_STATE_NUDGEUP = -30;
	protected static int UI_STATE_NUDGEDOWN = 30;
	
	protected static int CLOSE_BOX_SIZE = 14;

	protected Group uiGroup = null;
	protected Text txtTitleEdit = null;
	protected InitiativeManager initMgr = null;
	protected Menu contextMenu = null;
	protected Menu attributeSubmenu = null;
	protected Menu statusSubmenu = null;
	
	@Expose(serialize = true, deserialize = true)
	protected String character = null;
	
	@Expose(serialize = true, deserialize = true)
	protected ArrayList<ValueLabel> attributes = new ArrayList<ValueLabel>();
	@Expose(serialize = true, deserialize = true)
	protected HashMap<String, StatusLabel> statuses = new HashMap<String, StatusLabel>();
	
	protected Point priorLoc = null; // Tracked in the Parent coordinate grid
										// (InitiativeManager)
	protected int uiState = UI_STATE_NORMAL;
	protected Group dragShadow = null;
	protected Rectangle closeBox = null;
	protected InitiativeDisplayGroup me = null;

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

	/**
	 * @param state
	 */
	public void setUiState(int state) {
		uiState = state;
	}

	/**
	 * @param text
	 * @param parent
	 */
	public InitiativeDisplayGroup(String charName, InitiativeManager parent) {
		me = this;  // Workaround since "this" cannot be referenced within anonymous classes.
		character = charName;
		initMgr = parent;
		uiGroup = new Group(initMgr.getCharacterWindow(), SWT.VERTICAL);
		uiGroup.addMouseListener(this);
		uiGroup.addMouseMoveListener(this);
		uiGroup.setText(character);
		uiGroup.addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(DisposeEvent e) {
				for (ValueLabel vl : attributes) {
					vl.getValueLabelControl().dispose();
				}

			}
		});

		menuConfig();
		
		// Arrange the card such that attributes show in columns.
		RowLayout cardContentLayout = new RowLayout();
		cardContentLayout.type = SWT.VERTICAL;
		uiGroup.setLayout(cardContentLayout);

		// Set up a text edit box for changing character names.
		txtTitleEdit = new Text(uiGroup, SWT.NONE);
		txtTitleEdit.setVisible(false);
		txtTitleEdit.addTraverseListener(new TraverseListener() {

			@Override
			public void keyTraversed(TraverseEvent e) {
				character = txtTitleEdit.getText();
				uiGroup.setText(character);
				txtTitleEdit.setVisible(false);

			}
		});

		// Exclude this title edit control from being automatically organized in the layout
		RowData data = new RowData();
		data.exclude = true;
		txtTitleEdit.setLayoutData(data);

		// Draw a "close" box.
		uiGroup.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				// Set up the area which represents the close box.
				int boxsize = (int) (CLOSE_BOX_SIZE * parent.getScale());
				Rectangle clientArea = uiGroup.getClientArea();
				closeBox = new Rectangle(clientArea.width + clientArea.x - boxsize,
						(int) (e.gc.getFontMetrics().getHeight() / 2), boxsize, boxsize);
				e.gc.drawRectangle(closeBox);
				
				// Drawn an "X" in the rectangle just created.
				e.gc.drawLine(closeBox.x, closeBox.y, closeBox.x + closeBox.width, closeBox.y + closeBox.height);
				e.gc.drawLine(closeBox.x, closeBox.y + closeBox.height, closeBox.x + closeBox.width, closeBox.y);
				
				// Draw any status labels that apply
				Point startLoc = new Point(uiGroup.getSize().x - (closeBox.width + 5), 0);
				for (StatusLabel statLbl : statuses.values()) {
					startLoc = statLbl.paint(e, parent.getScale(), startLoc);
				}
				
			}
		});

	}

	/**
	 * Create the right-click menus for the group.
	 */
	protected void menuConfig() {
		// Set up a right-click context menu.
		contextMenu = new Menu(getControl());
		uiGroup.setMenu(contextMenu);
		
		// Menu for editing the name.
		MenuItem addEditTitleMenu = new MenuItem(contextMenu, SWT.CASCADE);
		addEditTitleMenu.setText("Edit Character Name");
		addEditTitleMenu.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				log.debug("Selected: " + addEditTitleMenu);
				editTitle();
			}
		});	
		
		MenuItem removeChar = new MenuItem(contextMenu, SWT.CASCADE);
		removeChar.setText("Remove Character");
		removeChar.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				log.debug("Selected: " + removeChar);
				initMgr.removeCharacterCard(me);
			}
		});
		
		new MenuItem(contextMenu, SWT.SEPARATOR);
		
		// Menu for adding a new attribute.
		MenuItem addAttrMenu = new MenuItem(contextMenu, SWT.CASCADE);
		addAttrMenu.setText("Add Attribute");
		addAttrMenu.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				log.debug("Selected: " + addAttrMenu);
				ValueLabel vl = addAttribute(me, "New Attribute", "");
				vl.editName(true);
			}
		});			
		
		// Menu for removing attributes
		MenuItem remAttrMenu = new MenuItem(contextMenu, SWT.CASCADE);
		remAttrMenu.setText("Remove Attribute");
		attributeSubmenu = new Menu(contextMenu);
		remAttrMenu.setMenu(attributeSubmenu);
		remAttrMenu.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				log.debug("Selected: " + remAttrMenu);
			}
		});
		
		new MenuItem(contextMenu, SWT.SEPARATOR);
		
		// Menu for adding statuses
		MenuItem statusMenu = new MenuItem(contextMenu, SWT.CASCADE);
		statusMenu.setText("Toggle Status");
		statusSubmenu = new Menu(contextMenu);
		statusMenu.setMenu(statusSubmenu);
		statusMenu.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				log.debug("Selected: " + statusMenu);
			}
		});
		
		MenuItem clearAll = new MenuItem(statusSubmenu, SWT.CASCADE);
		clearAll.setText("Clear All");
		new MenuItem(statusSubmenu, SWT.SEPARATOR);
		clearAll.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				for (String text : initMgr.getStatusMetadata().keySet()) {
					StatusMetadata statMeta = initMgr.getStatusMetadata().get(text);
					statusOff(statMeta);
				}
				syncStatusMenuState();
				redraw();
			}
		});
		
		loadStatusMenu();
		
	}
	
	/**
	 * @return
	 */
	public String getCharacter() {
		return character;
	}

	/**
	 * Add a new attribute to the collect, and handle any setup necessary.
	 * 
	 * @param parent
	 * @param label
	 * @param value
	 * @return
	 */
	public ValueLabel addAttribute(InitiativeDisplayGroup parent, String label, String value) {
		ValueLabel newAttr = new ValueLabel(parent, label, value);
		newAttr.setMenu(contextMenu);
		attributes.add(newAttr);
		
		MenuItem attribute = new MenuItem(attributeSubmenu, SWT.CASCADE);
		attribute.setText(newAttr.getName());
		attribute.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				log.debug("Selected: " + attribute);
				removeAttribute(newAttr);
			}
		});
		
		newAttr.positionControls(initMgr.getScale());
		
		return newAttr;
	}
	
	/**
	 * Get rid of an attribute.
	 * @param vl
	 */
	public void removeAttribute(ValueLabel vl) {
		attributes.remove(vl);
		vl.setMenu(null);
		vl.getValueLabelControl().setVisible(false);
		vl.getValueLabelControl().dispose();
	}
	
	/**
	 * Reads the default initiative attributes from a file and sets up empty
	 * labels for them.
	 */
	public void loadDefaultAttributes() {
		// Load attributes for this initiative card from the default file.

		try {
			// TODO: Make default property file name configurable.
			Scanner attributeFile = new Scanner(LaunchPad.class.getResourceAsStream("/com/deathfrog/utils/DefaultAttributes.json"));
			if (attributeFile != null) {
				JsonArray ja = JsonUtils.readJsonStream(attributeFile, "attributes");
				log.info(ja);
				for (JsonElement je : ja) {
					log.info(je);
					if (je.isJsonObject()) {
						String attribute = JsonUtils.getJsonString(je, "label");
						addAttribute(this, attribute, "");
					}
				}
			}
		} catch (FileNotFoundException fe) {
			// TODO: Display error messages to the user when appropriate.
			log.error(GameException.fullExceptionInfo(fe));
		} catch (IOException ie) {
			// TODO: Display error messages to the user when appropriate.
			log.error(GameException.fullExceptionInfo(ie));
		}
	}
	
	/**
	 * Set up the possible statuses that can be applied to a character.
	 */
	public void loadStatusMenu() {
		// Load attributes for this initiative card from the status metadata.
		for (String text : initMgr.getStatusMetadata().keySet()) {
			StatusMetadata statMeta = initMgr.getStatusMetadata().get(text);
			MenuItem miStatus = new MenuItem(statusSubmenu, SWT.CASCADE);
			miStatus.setText(text);
			miStatus.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					log.debug("Selected: " + miStatus);
					boolean on = toggleStatus(statMeta);
					if (on) {
						miStatus.setImage(StatusLabel.selectedIcon);
					} else {
						miStatus.setImage(StatusLabel.unselectedIcon);	
					}
				}
			});
			
		}
	}


	/**
	 * cycle through all status submenu items and set up the selected/unselected icon appropriately based on the current state.
	 */
	protected void syncStatusMenuState() {
		for (MenuItem miStatus : statusSubmenu.getItems()) {
			
			// This allows the menu to correctly display status selection icons after initial load from save state;
			if (me.statuses.containsKey(miStatus.getText())) {
				miStatus.setImage(StatusLabel.selectedIcon);
			} else{
				// Set the unselected icon only if the menu text is the name of a valid status.  (Allows "clear all" to have no icon.)
				if (initMgr.getStatusMetadata().keySet().contains(miStatus.getText())) {
					miStatus.setImage(StatusLabel.unselectedIcon);	
				}
			}
		}
	}
	
	/**
	 * @param statusName
	 */
	protected void statusOn(StatusMetadata statMeta) {
		StatusLabel addedStatus = new StatusLabel(this);
		addedStatus.setStatMeta(statMeta);
		statuses.put(statMeta.getName(), addedStatus);		
	}
	
	/**
	 * @param statusName
	 */
	protected void statusOff(StatusMetadata statMeta) {
		statuses.remove(statMeta.getName());
		SWTResourceManager.releaseColorResource(statMeta.getSWTColor(null));
	}
	
	/**
	 * 
	 */
	protected void redraw() {
		//layout of parent works
		uiGroup.getParent().layout(true, true);

		//marks the composite's screen are as invalidates, which will force a redraw on next paint request 
		uiGroup.redraw(); 

		//tells the application to do all outstanding paint requests immediately
		uiGroup.update(); 		
	}
	
	/**
	 * @param text
	 * @param color
	 * @return
	 */
	protected boolean toggleStatus(StatusMetadata statMeta) {
		boolean on = false;
		if (statuses.containsKey(statMeta.getName())) {
			statusOff(statMeta);
		} else {
			statusOn(statMeta);
			on = true;
		}
		
		redraw();
		
		return on;
	}
	
	/**
	 * set the title box active for editing.
	 */
	public void editTitle() {
		log.info("Editing title.");
		txtTitleEdit.setText(character);
		txtTitleEdit.setVisible(true);
		txtTitleEdit.forceFocus();
		txtTitleEdit.selectAll();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt.
	 * events.MouseEvent)
	 */
	@Override
	public void mouseDoubleClick(MouseEvent e) {
		// no-op
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events.
	 * MouseEvent)
	 */
	@Override
	public void mouseDown(MouseEvent e) {
		// log.debug(e);
		Point pt = new Point(e.x, e.y);
		Device device = Display.getCurrent();
		
		// A left-mouse click while no drag action is occurring initiates a drag
		// action.
		if (uiState == UI_STATE_NORMAL && e.button == LEFT_BUTTON) {
			if (closeBox.contains(pt)) {
				initMgr.removeCharacterCard(this);
			} else {
				log.info("Start Drag: " + uiGroup);
				uiState = UI_STATE_DRAG;

				Cursor cursor = new Cursor(device, SWT.CURSOR_HAND);
				uiGroup.setCursor(cursor);

				priorLoc = Display.getCurrent().map(uiGroup, initMgr.getCharacterWindow(), e.x, e.y);
				log.info("priorLoc: " + priorLoc);

				dragShadow = new Group(initMgr.getCharacterWindow(), uiGroup.getStyle());
				dragShadow.setText(character);
				dragShadow.setBounds(uiGroup.getBounds());

				// Put the dragShadow on top of everything else...
				dragShadow.moveAbove(null);

				uiGroup.setVisible(false);
			}
		}

		initMgr.childEventHandler(this, e);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.
	 * MouseEvent)
	 */
	@Override
	public void mouseUp(MouseEvent e) {
		Device device = Display.getCurrent();

		if (uiState == UI_STATE_DRAG && e.button == LEFT_BUTTON) {
			log.info("Stop Drag: " + uiGroup);

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events
	 * .MouseEvent)
	 */
	@Override
	public void mouseMove(MouseEvent e) {

		if (uiState == UI_STATE_DRAG) {
			Point eventPt = Display.getCurrent().map(uiGroup, initMgr.getCharacterWindow(), e.x, e.y);
			Point offset = new Point(eventPt.x - priorLoc.x, eventPt.y - priorLoc.y);
			Point newLoc = new Point(dragShadow.getBounds().x + offset.x, dragShadow.getBounds().y + offset.y);

			for (InitiativeDisplayGroup idg : initMgr.getInitiativeGroups()) {
				if (!this.equals(idg)) {
					Control c = idg.getControl();
					if (c.getBounds().y < newLoc.y && !(idg.getUiState() == UI_STATE_NUDGEUP)) {
						idg.setUiState(UI_STATE_NUDGEUP);
						Point nudgeLoc = new Point(c.getBounds().x, c.getBounds().y + UI_STATE_NUDGEUP);
						c.setLocation(nudgeLoc);
					}
					if (c.getBounds().y > newLoc.y && !(idg.getUiState() == UI_STATE_NUDGEDOWN)) {
						idg.setUiState(UI_STATE_NUDGEDOWN);
						Point nudgeLoc = new Point(c.getBounds().x, c.getBounds().y + UI_STATE_NUDGEDOWN);
						c.setLocation(nudgeLoc);
					}

					c.requestLayout();
				}
			}

			dragShadow.setLocation(newLoc);

			priorLoc = eventPt;
			dragShadow.requestLayout();
		} else {
			// Show the tool tips for the status markers
			boolean found = false;
			for (StatusLabel statLbl : statuses.values()) {
				if (statLbl.getStatusDisplayArea().contains(new Point(e.x, e.y))) {
					uiGroup.setToolTipText(statLbl.getStatMeta().toString());
					found = true;
					break;
				}
			}
			
			// Show the tool tip for the close box
			if (closeBox.contains(new Point(e.x, e.y))) {
				uiGroup.setToolTipText("Remove this character from the board.");
				found = true;
			}
			
			if (!found) {
				uiGroup.setToolTipText(null);
			}
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
		log.debug("Sibling event handler: " + childEvent);
		attributeEditEvent(null); // Any mouse event from a sibling should end in edits in progress.
		txtTitleEdit.setVisible(false); // And stop an in-progress name edit.
	}

	/**
	 * Adjust the contents of the card for the scale they're being shown at, and
	 * mark items on the card for layout refresh.
	 */
	public void requestLayout(double scale) {
		// Set up the size of all of the attribute labels.
		for (ValueLabel vl : attributes) {
			vl.positionControls(scale);
		}

		// Set up the size of the (usually invisible) title box, so it can be
		// used to detect mouse events.
		txtTitleEdit.setFont(uiGroup.getFont());
		GC gc = new GC(uiGroup);
		Point valueSize = gc.textExtent(character);
		gc.dispose();
		Rectangle editTrim = txtTitleEdit.computeTrim(6, 0, valueSize.x, valueSize.y);
		txtTitleEdit.setBounds(editTrim);

		uiGroup.requestLayout();
	}


	/**
	 * Allows one attribute ("source") to signal the other attributes in the
	 * group that any edit in progess should end.
	 * 
	 * @param source
	 */
	protected void attributeEditEvent(ValueLabel source) {
		for (ValueLabel vl : attributes) {
			if (!vl.equals(source)) {
				vl.editValue(false);
				vl.editName(false);
			}
		}
	}

}
