package com.deathfrog.utils.ui;


import java.io.FileWriter;
import java.io.IOException;
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
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;

import com.deathfrog.utils.GameException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;



/**
 * @author Al Mele
 *
 */
public class InitiativeManager {
	protected static Logger log = LogManager.getLogger(InitiativeManager.class);
	protected int ANCHOR_X = 20;
	protected int ANCHOR_Y = 40;
	protected double CARD_WIDTH = 240.0;
	protected double CARD_HEIGHT = 120.0;
	protected double FONT_HEIGHT = 12.0;
	protected double scale = 1.0;
	protected double priorScale = scale;
	
	protected Shell imShell = null;
	protected ScrolledComposite viewPort = null;
	protected Composite characterWindow = null;
	protected HashMap<Control, InitiativeDisplayGroup> controlMap = new HashMap<Control, InitiativeDisplayGroup>();
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
		
		Spinner zoomSpinner = new Spinner(imShell, SWT.NONE);
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
		viewPort.setBounds(0, 0, imShell.getBounds().width - 14, imShell.getBounds().height);
		viewPort.setExpandHorizontal( true );
		viewPort.setExpandVertical( true );
		
		characterWindow = new Composite(viewPort, SWT.NONE);
		// characterWindow.setLayout(new FillLayout(SWT.VERTICAL));
		viewPort.setContent(characterWindow);
		
		viewPort.setMinSize(getChildrenMaxLocation());

		imShell.addListener( SWT.Resize, event -> {
			manageSizing();
			} );
		
		addPlayer.addMouseListener(new MouseListener() {

			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}

			@Override
			public void mouseDown(MouseEvent e) {
				int listsize = controlMap.size();
				int xPos = ANCHOR_X;
				int yPos = (int) (ANCHOR_Y + (CARD_HEIGHT * listsize * scale));
				InitiativeDisplayGroup characterInitiativeCard = new InitiativeDisplayGroup("New Character " + (listsize+1), getInitiativeManager());
				characterInitiativeCard.setBounds(xPos, yPos, (int)(CARD_WIDTH * scale), (int)(CARD_HEIGHT * scale));
				controlMap.put(characterInitiativeCard.getControl(), characterInitiativeCard);
				idgList.add(characterInitiativeCard);
				manageSizing();
			}

			@Override
			public void mouseUp(MouseEvent e) {
			}

		});		
		
		return imShell;
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
	 * @param childEvent
	 */
	public void childEventHandler(InitiativeDisplayGroup source, MouseEvent childEvent) {
		// Point parentLoc = Display.getCurrent().map(source.getControl(), characterWindow, childEvent.x, childEvent.y);

		// Report a mouse event from one child to all siblings
		for (InitiativeDisplayGroup idg : idgList) {
			if (!source.equals(idg)) {
				idg.siblingEventHandler(source, childEvent);
			}
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
		
		Point pt = new Point(x, y);
		
		return pt;
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
				c.setBounds(c.getBounds().x, c.getBounds().y, (int)(CARD_WIDTH * scale), (int)(CARD_HEIGHT * scale));
				FontData[] fD = c.getFont().getFontData();
				fD[0].setHeight((int) (FONT_HEIGHT * scale));
				
	
				SWTResourceManager.releaseFontResource(c.getFont());
				c.setFont(SWTResourceManager.createFontResource(c.getDisplay(),fD[0]));		     	
				c.requestLayout();
				
				InitiativeDisplayGroup idg = controlMap.get(c);
				if (idg != null) {
					idg.requestLayout(scale);
				}
				
			}	
			
			priorScale = scale;
		}
		
		// determine how much room the character cards take up (max x and y)
		Point pt = getChildrenMaxLocation();
		
		// Set the viewport to the size of the parent shell.
		viewPort.setBounds(0, 0, imShell.getBounds().width - 14 /* Scrollbar width - inelegant */, imShell.getBounds().height);
		
		// Set the minimum size so the viewport can decide if scrolling needs to be allowed.
		pt.y = pt.y + 30;  // Account for Shell menu space when setting viewport minimum size. (Inelegant).
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
			i++;
		}
		
	}
	
	/**
	 * Saves the current configuration of the initiative board to a JSON file.
	 */
	public void persistContent() {
		HashMap<String, HashMap<String, String>> persistMap = new HashMap<String, HashMap<String, String>>();
		for (InitiativeDisplayGroup idg : idgList) {
			HashMap<String, String> attributeList = new HashMap<String, String>();
			for (ValueLabel attribute : idg.getAttributes()) {
				attributeList.put(attribute.getText(), attribute.getValue());
			}
			persistMap.put(idg.getText(), attributeList);
		}
		
		log.info(persistMap);
		
		try (Writer writer = new FileWriter("Output.json")) {
		    Gson gson = new GsonBuilder().create();
		    gson.toJson(persistMap, writer);
		} catch (IOException ie) {
			// TODO: Display error messages to the user when appropriate.
			log.error(GameException.fullExceptionInfo(ie));
		}		
	}
}
