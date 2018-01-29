package com.deathfrog.ui.initiative;


import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;

import com.deathfrog.utils.GameException;
import com.deathfrog.utils.ui.LaunchPad;
import com.deathfrog.utils.ui.SWTResourceManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;



/**
 * @author Al Mele
 *
 */
public class InitiativeManager {
	protected static Logger log = LogManager.getLogger(InitiativeManager.class);
	protected int ANCHOR_X = 50;
	protected int ANCHOR_Y = 10;
	protected double CARD_WIDTH = 320.0;
	protected double CARD_HEIGHT = 90.0;
	protected double FONT_HEIGHT = 12.0;
	protected int CONTROLBAR_HEIGHT = 40;
	
	@Expose(serialize = true, deserialize = true)
	protected double scale = 1.0;
	Spinner zoomSpinner = null;
	
	@Expose(serialize = true, deserialize = true)
	protected int turnIndex = 0;
	
	protected double priorScale = scale;
	protected InitiativeDisplayGroup priorEventSource = null;
	
	protected Shell imShell = null;
	protected ScrolledComposite viewPort = null;
	protected Composite characterWindow = null;
	protected HashMap<Control, InitiativeDisplayGroup> controlMap = new HashMap<Control, InitiativeDisplayGroup>();
	protected Image turnArrow = null;
	
	@Expose(serialize = true, deserialize = true)
	protected ArrayList<InitiativeDisplayGroup> idgList = new ArrayList<InitiativeDisplayGroup>();
	
	protected Font fontCreatedForScaling = null;  // Track for disposal
	
	/**
	 * @return
	 */
	protected InitiativeManager getInitiativeManager() {
		return this;
	}
	
	/**
	 * @return
	 */
	public double getScale() {
		return scale;
	}
	
	/**
	 * @return
	 */
	public Image getTurnArrow() {
		return turnArrow;
	}
	
	/**
	 * @return
	 */
	/**
	 * @wbp.parser.entryPoint
	 */
	public Shell createContents() {
		
		imShell = new Shell();
		imShell.setImage(LaunchPad.getIcon());
		imShell.setSize(800, 540);
		imShell.setText("Initiative Manager");
		imShell.addShellListener(new ShellListener(){

			@Override
			public void shellActivated(ShellEvent e) {
				// no-op
				
			}

			@Override
			public void shellClosed(ShellEvent e) {
				persistContent();
				
			}

			@Override
			public void shellDeactivated(ShellEvent e) {
				// no-op
				
			}

			@Override
			public void shellDeiconified(ShellEvent e) {
				// no-op
				
			}

			@Override
			public void shellIconified(ShellEvent e) {
				// no-op
				
			}});
		
		Button addPlayer = new Button(imShell, SWT.NONE);
		addPlayer.setBounds(14, 14, 120, 20);
		addPlayer.setText("Add Character");
		
		Label lblZoom = new Label(imShell, SWT.NONE);
		lblZoom.setText("Zoom");
		lblZoom.setBounds(160, 14, 40, 20);
		
		Button prevTurn = new Button(imShell, SWT.NONE);
		prevTurn.setBounds(280, 14, 60, 20);
		prevTurn.setText("Prev.");	
		
		Button nextTurn = new Button(imShell, SWT.NONE);
		nextTurn.setBounds(350, 14, 60, 20);
		nextTurn.setText("Next");		
		
		zoomSpinner = new Spinner(imShell, SWT.NONE);
		zoomSpinner.setBounds(200, 14, 60, 20);
		zoomSpinner.setMaximum(1000);
		zoomSpinner.setMinimum(100);
		zoomSpinner.setSelection(100);
		zoomSpinner.setIncrement(10);
		zoomSpinner.addModifyListener(event -> {
			Double spinVal = new Double(zoomSpinner.getSelection());
			scale = (spinVal / 100.0);
			manageSizing();
			} );
		
		viewPort = new ScrolledComposite( imShell, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER );
		viewPort.setBounds(0, CONTROLBAR_HEIGHT, imShell.getBounds().width - 14, imShell.getBounds().height);
		viewPort.setExpandHorizontal( true );
		viewPort.setExpandVertical( true );
		
		characterWindow = new Composite(viewPort, SWT.NONE);
		
		// Draw the turn arrows.
		characterWindow.addPaintListener(new PaintListener() {

			@Override
			public void paintControl(PaintEvent pEv) {
				Control turnCtl = idgList.get(turnIndex).getControl();
				Rectangle idgSpot = turnCtl.getBounds();
				pEv.gc.drawImage(getTurnArrow(),0,idgSpot.y + 10);
				viewPort.showControl(turnCtl);
			}}); 

		
		viewPort.setContent(characterWindow);
		
		viewPort.setMinSize(getChildrenMaxLocation());

		imShell.addListener( SWT.Resize, event -> {
			manageSizing();
			} );
		
		// Set up the functionality to add new character initiative cards.
		addPlayer.addMouseListener(new MouseListener() {

			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}

			@Override
			public void mouseDown(MouseEvent e) {
				int listsize = controlMap.size();
				InitiativeDisplayGroup idg = addCharacterCard("New Character " + (listsize+1));
				idg.loadDefaultAttributes();
				manageSizing();
				idg.editTitle();
			}

			@Override
			public void mouseUp(MouseEvent e) {
			}

		});		
		
		// Iterate through the turns (backward)
		prevTurn.addMouseListener(new MouseListener() {

			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}

			@Override
			public void mouseDown(MouseEvent e) {
				if (turnIndex == 0) {
					turnIndex = idgList.size() - 1;
				} else {
					turnIndex--;
				}
				characterWindow.redraw();
			}

			@Override
			public void mouseUp(MouseEvent e) {
			}

		});		
		// Iterate through the turns (forward)
		nextTurn.addMouseListener(new MouseListener() {

			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}

			@Override
			public void mouseDown(MouseEvent e) {
				if (turnIndex == idgList.size() - 1) {
					turnIndex = 0;
				} else {
					turnIndex++;
				}
				characterWindow.redraw();
			}

			@Override
			public void mouseUp(MouseEvent e) {
			}

		});			
		
		// TODO: Make this configurable.
		turnArrow = new Image(imShell.getDisplay(), 
			    InitiativeManager.class.getResourceAsStream(
			      "/turnArrow.png"));
		
		readContent();
		
		return imShell;
	}
	

	/**
	 * Add a new character card to the list.
	 * @param name
	 * @return
	 */
	protected InitiativeDisplayGroup addCharacterCard(String name) {
		int listsize = controlMap.size();
		int xPos = ANCHOR_X;
		int yPos = (int) (ANCHOR_Y + (CARD_HEIGHT * listsize * scale));
		InitiativeDisplayGroup characterInitiativeCard = new InitiativeDisplayGroup(name, getInitiativeManager());
		characterInitiativeCard.setBounds(xPos, yPos, (int)(CARD_WIDTH * scale), (int)(CARD_HEIGHT * scale));
		controlMap.put(characterInitiativeCard.getControl(), characterInitiativeCard);
		idgList.add(characterInitiativeCard);
		scaleControl(characterInitiativeCard.getControl(), scale);
		
		return characterInitiativeCard;
	}
	
	/**
	 * Drop a character initiative group and remove resources associated with it.
	 * @param initiativeDisplayGroup
	 */
	public void removeCharacterCard(InitiativeDisplayGroup initiativeDisplayGroup) {
		controlMap.remove(initiativeDisplayGroup.getControl());
		idgList.remove(initiativeDisplayGroup);
		initiativeDisplayGroup.getControl().dispose();	
		manageSizing();
	}	
	
	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		imShell = createContents();
		imShell.open();
		imShell.layout();
		while (!imShell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}
	
	/**
	 * 
	 */
	public void close() {
		
		if (imShell != null && !imShell.isDisposed()) {
			imShell.close();
		}
	}
	
	/**
	 * @return
	 */
	public Shell getShell() {
		return imShell;
	}
	
	/**
	 * @return
	 */
	public Composite getCharacterWindow() {
		return characterWindow;
	}
	
	/**
	 * @return
	 */
	public boolean isOpen() {
		return (imShell != null && !imShell.isDisposed());
	}
	
	/**
	 * Called when a character initiative card received a mouse event.
	 * @param childEvent
	 */
	public void childEventHandler(InitiativeDisplayGroup source, MouseEvent childEvent) {
		// Point parentLoc = Display.getCurrent().map(source.getControl(), characterWindow, childEvent.x, childEvent.y);

		if (!source.equals(priorEventSource)) {
			// Report a mouse event from one child to all siblings, the first time it is coming from a new source.
			for (InitiativeDisplayGroup idg : idgList) {
				if (!source.equals(idg)) {
					log.debug("childEventHandler: " + source);
					idg.siblingEventHandler(source, childEvent);
				}
			}
			
			priorEventSource = source;
		}
	}
	

	/**
	 * @param p - the point (in the coordinate system of the InitiativeManager) to check for children.
	 * @return
	 */
	public ArrayList<Control> childrenAtPoint(Point p) {
		ArrayList<Control> children = new ArrayList<Control>();
		
        for (Control control : characterWindow.getChildren()) {
            if (control.getBounds().contains(p)) {
	            // log.debug("Control Identified: " + control);
	            children.add(control);
            }
        }
		
		return children;
	}

	
	/**
	 * @return
	 */
	public ArrayList<InitiativeDisplayGroup> getInitiativeGroups() {
		return idgList;
	}
	
	/**
	 * Examine the locations of all children cards and report back a Point that represents the 
	 * maximum x and y positions needed in order to see all cards.
	 * 
	 * @return
	 */
	public Point getChildrenMaxLocation() {
		int x = -1, y = -1;
		
		for (Control c : characterWindow.getChildren()) {
			if ((c.getBounds().x + c.getBounds().width) > x) {
				x = (c.getBounds().x + c.getBounds().width);
			}
			if ((c.getBounds().y + c.getBounds().height) > y) {
				y = (c.getBounds().y + c.getBounds().height);
			}			
		}
		
		Point pt = new Point(x, y + CONTROLBAR_HEIGHT);
		
		return pt;
	}
	
	/**
	 * Given a control, scale it to the zoom scale specified for the interface.
	 * 
	 * @param c
	 * @param zoomScale
	 */
	protected void scaleControl(Control c, double zoomScale) {
		c.setBounds(c.getBounds().x, c.getBounds().y, (int)(CARD_WIDTH * scale), (int)(CARD_HEIGHT * zoomScale));
		FontData[] fD = c.getFont().getFontData();
		fD[0].setHeight((int) (FONT_HEIGHT * zoomScale));
		

		SWTResourceManager.releaseFontResource(c.getFont());
		c.setFont(SWTResourceManager.createFontResource(c.getDisplay(),fD[0]));		     	
		c.requestLayout();
		
		InitiativeDisplayGroup idg = controlMap.get(c);
		if (idg != null) {
			idg.requestLayout(zoomScale);
		}
	}
	
	/**
	 * Take care of whatever maintenance is associated with managing window sizes.
	 */
	protected void manageSizing() {
		log.debug("Managing sizing...");
		
		// Handle scaling only if the scale has actually changed...
		if (priorScale != scale) {
			
			// Handle scaling of the character cards, and ask that they in turn scale their contents.
			for (Control c : characterWindow.getChildren()) {
				scaleControl(c, scale);
			}	
			
			priorScale = scale;
		}
		
		// determine how much room the character cards take up (max x and y)
		Point pt = getChildrenMaxLocation();
		
		// Set the viewport to the size of the parent shell.
		viewPort.setBounds(0, CONTROLBAR_HEIGHT, imShell.getBounds().width - 14 /* Scrollbar width - inelegant */, (imShell.getBounds().height - CONTROLBAR_HEIGHT));
		
		// Set the minimum size so the viewport can decide if scrolling needs to be allowed.
		viewPort.setMinSize(pt);	
		
		straightenCards();
	}
	
	/**
	 * Organize the cards based on their sorted order.
	 */
	public void straightenCards() {
		// Order the character initiative cards based on their relative Y positions
		idgList.sort(new Comparator<InitiativeDisplayGroup>() {
			@Override
			public int compare(InitiativeDisplayGroup lhs, InitiativeDisplayGroup rhs) {
				int comparator = 0;
		        // -1 - less than, 1 - greater than, 0 - equal
				if (lhs.getControl().getBounds().y == rhs.getControl().getBounds().y) {
					comparator = 0;
				} else if (lhs.getControl().getBounds().y < rhs.getControl().getBounds().y) {
					comparator = -1;
				} else if (lhs.getControl().getBounds().y > rhs.getControl().getBounds().y) {
					comparator = 1;
				}
				
				// log.info(((Group) lhs.getControl()) + ": " 	+ lhs.getControl().getBounds().y + ", "	+ ((Group) rhs.getControl()) + ": " + rhs.getControl().getBounds().y + ": "	+ comparator);
				
		        return comparator;
			}
		}
		);
				
		int i = 0;
		
		for (InitiativeDisplayGroup idg : idgList) {
			int xPos = ANCHOR_X;
			int yPos = (int) (ANCHOR_Y + (CARD_HEIGHT * i * scale));
			idg.setBounds(xPos, yPos, (int)(CARD_WIDTH * scale), (int)(CARD_HEIGHT * scale));	
			idg.setUiState(InitiativeDisplayGroup.UI_STATE_NORMAL);
			i++;
		}
		
	    characterWindow.redraw();
	}
	
	/**
	 * Saves the current configuration of the initiative board to a JSON file.
	 */
	public void persistContent() {
		
		try (Writer writer = new FileWriter("Output.json")) {
		    final GsonBuilder builder = new GsonBuilder();
		    builder.excludeFieldsWithoutExposeAnnotation();
		    final Gson gson = builder.create();
		    // gson.toJson(persistMap, writer);
		    gson.toJson(this, writer);
		} catch (IOException ie) {
			// TODO: Display error messages to the user when appropriate.
			log.error(GameException.fullExceptionInfo(ie));
		}		
	}
	
	/**
	 * Read the prior state from the file.
	 */
	public void readContent() {
		// TODO: Make save state file name configurable.
		try (Reader reader = new FileReader("Output.json")) {
		    final GsonBuilder builder = new GsonBuilder();
		    builder.excludeFieldsWithoutExposeAnnotation();
		    final Gson gson = builder.create();
		    // gson.toJson(persistMap, writer);
		    InitiativeManager dummyManager = gson.fromJson(reader, InitiativeManager.class);
		    this.scale = dummyManager.scale;
		    this.turnIndex = dummyManager.turnIndex;
		    
		    zoomSpinner.setSelection((int)(scale * 100));
		    
		    for (InitiativeDisplayGroup dummyGroup : dummyManager.getInitiativeGroups()) {
		    	InitiativeDisplayGroup idg = this.addCharacterCard(dummyGroup.getCharacter());
		    	
		    	for (ValueLabel vl : dummyGroup.getAttributes()) {
		    		idg.addAttribute(idg, vl.getName(), vl.getValue());
		    	}
		    	idg.requestLayout(scale);
		    }
		    
		    manageSizing();

		} catch (JsonSyntaxException jse) {
			// TODO: Display error messages to the user when appropriate.
			log.error(GameException.fullExceptionInfo(jse));
		} catch (IOException ie) {
			// TODO: Display error messages to the user when appropriate.
			log.error(GameException.fullExceptionInfo(ie));
		}
	}


}
