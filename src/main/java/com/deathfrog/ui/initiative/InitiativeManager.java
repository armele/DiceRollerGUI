package com.deathfrog.ui.initiative;


import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Scanner;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;

import com.deathfrog.utils.GameException;
import com.deathfrog.utils.JsonUtils;
import com.deathfrog.utils.dice.Die;
import com.deathfrog.utils.ui.LaunchPad;
import com.deathfrog.utils.ui.SWTResourceManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;


/**
 * Inner class for serialization of JSON info about window position.
 * 
 * @author Al Mele
 *
 */
class WindowPosition implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Expose(serialize = true, deserialize = true)
	protected int x;
	@Expose(serialize = true, deserialize = true)
	protected int y;
	@Expose(serialize = true, deserialize = true)
	protected int width;
	@Expose(serialize = true, deserialize = true)
	protected int height;
	
	public void setFromRectangle(Rectangle bounds) {
		x = bounds.x;
		y = bounds.y;
		width = bounds.width;
		height = bounds.height;
	}
	
	/**
	 * Is this a valid window position?
	 * @return
	 */
	public boolean isValid() {
		boolean valid = false;
		
		if (x != 0 && y != 0 && width > 100 && height > 100) {
			valid = true;
		}
		
		return(valid);
	}
}

/**
 * @author Al Mele
 *
 */
public class InitiativeManager {
	protected static Logger log = LogManager.getLogger(InitiativeManager.class);
	protected static int ANCHOR_X = 50;
	protected static int ANCHOR_Y = 10;
	protected static double CARD_WIDTH = 320.0;
	protected static double CARD_HEIGHT = 90.0;
	protected static double CARD_SKINNY_HEIGHT = 30.0;
	protected static double FONT_HEIGHT = 12.0;
	protected static final int LBL_FONT_HEIGHT = 8;
	protected static final int SROLLBAR_WIDTH = 18;
	protected static double ROLL_FONT_HEIGHT = 18.0;
	protected static int CONTROLBAR_HEIGHT = 40;
	protected static int READY_INSET = 40;
	
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
	protected Combo cmbRollChoice = null;
	protected HashMap<Control, InitiativeDisplayGroup> controlMap = new HashMap<Control, InitiativeDisplayGroup>();
	protected TreeMap<String, StatusMetadata> statusMetadata = new TreeMap<String, StatusMetadata>();
	protected Image turnArrow = null;
	protected Composite tavern = null;
	
	@Expose(serialize = true, deserialize = true)
	protected ArrayList<InitiativeDisplayGroup> idgList = new ArrayList<InitiativeDisplayGroup>();

	@Expose(serialize = true, deserialize = true)
	protected ArrayList<InitiativeDisplayGroup> tavernList = new ArrayList<InitiativeDisplayGroup>();	
	
	@Expose(serialize = true, deserialize = true)
	protected boolean skinnyView = false;
	
	@Expose(serialize = true, deserialize = true)	
	protected WindowPosition windowLocation = new WindowPosition();
	
	protected Image readyPicture = null;
	
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
	public TreeMap<String, StatusMetadata> getStatusMetadata() {
		return statusMetadata;
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
	public int getTurnIndex() {
		return turnIndex;
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
	public double getCardHeight() {
		return (skinnyView ? CARD_SKINNY_HEIGHT : CARD_HEIGHT );
	}
	
	/**
	 * Return the Tavern object.
	 * 
	 * @return
	 */
	public Composite getTavern() {
		return tavern;
	}

	/**
	 * 
	 */
	protected void createToolbarFunctions() {
		Button addPlayer = new Button(imShell, SWT.NONE);
		addPlayer.setBounds(14, 14, 120, 20);
		addPlayer.setText("Add Character");
		
		// Set up the functionality to add new character initiative cards.
		addPlayer.addMouseListener(new MouseListener() {

			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}

			@Override
			public void mouseDown(MouseEvent e) {
				int listsize = controlMap.size();
				InitiativeDisplayGroup idg = addCharacterCard("New Character " + (listsize+1), null, false);
				manageSizing();
				idg.editTitle();
			}

			@Override
			public void mouseUp(MouseEvent e) {
			}

		});		
		
		Label lblZoom = new Label(imShell, SWT.NONE);
		lblZoom.setText("Zoom");
		lblZoom.setBounds(160, 14, 40, 20);
		
		Button btnSkinnyView = new Button(imShell, SWT.CHECK);
		btnSkinnyView.setBounds(420, 14, 90, 20);
		btnSkinnyView.setText("Skinny View");	
		btnSkinnyView.setSelection(skinnyView);
		btnSkinnyView.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// no-op
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				skinnyView = !skinnyView;
				btnSkinnyView.setSelection(skinnyView);
				straightenCards();
			}});
		
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
		
		cmbRollChoice = new Combo(imShell, SWT.NONE);
		cmbRollChoice.setBounds(550, 14, 105, 23);

		Button btnRoll = new Button(imShell, SWT.NONE);
		btnRoll.setBounds(661, 14, 32, 25);
		btnRoll.setText("Roll");

		btnRoll.addMouseListener(new MouseListener() {

			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}

			@Override
			public void mouseDown(MouseEvent e) {
				if (cmbRollChoice.getText().length() == 0) {
					for (InitiativeDisplayGroup idg : idgList) {
						idg.setRollValue(0);
					}
					log.debug("Rolls cleared.");
				} else {
					for (InitiativeDisplayGroup idg : idgList) {
						Die d = new Die(20);
						idg.setRollValue(d.roll());
					}
					log.debug("Rolls made!");
				}
				
				manageSizing();
			}

			@Override
			public void mouseUp(MouseEvent e) {
			}

		});	
		
	}
	
	/**
	 * Set up the tavern context menu to list who is in the tavern.
	 */
	protected void syncTavernMenu() {
		for (MenuItem mi : tavern.getMenu().getItems()) {
			mi.dispose();
		}
		
		for (InitiativeDisplayGroup idg : tavernList) {
			MenuItem tavernNPC = new MenuItem(tavern.getMenu(), SWT.CASCADE);
			tavernNPC.setText(idg.getCharacter());
			tavernNPC.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					log.debug("Selected: " + tavernNPC);
					moveFromTavern(tavernNPC.getText());
				}
			});
		}
	}
	
	/**
	 * Set up the "tavern" - where initiative cards not needed for the current combat can hang out.
	 */
	protected void createTavern() {
		tavern = new Composite(characterWindow, SWT.NONE);
		Menu tavernContext = new Menu(tavern);
		tavern.setMenu(tavernContext);
		tavern.setToolTipText("Drag initiative cards here to relax and have a brew when they aren't participating in your combat. Right-click to retrieve.");
		tavern.addPaintListener(new PaintListener() {
			
			@Override
			public void paintControl(PaintEvent pEv) {
				String tavernImageFile = "tavern.png";
				Image tavernImage = SWTResourceManager.createImageResource(tavern, tavernImageFile);
				
				if (tavernImage != null) {
					pEv.gc.drawImage(tavernImage, 
							0, 0, tavernImage.getBounds().width, tavernImage.getBounds().height, // Source image information
							0, 0, tavern.getBounds().width, tavernImage.getBounds().height		 // Target location for image within the composite
							);
				} else {
					log.error("Image Missing: " + tavernImageFile);
				}
				
				if (tavernList.size() > 0) {
					Rectangle boundingBox = tavern.getBounds();
					Color backup = pEv.gc.getForeground();
					int backupLineWidth = pEv.gc.getLineWidth();
					pEv.gc.setForeground(pEv.display.getSystemColor(SWT.COLOR_GREEN));
					pEv.gc.setLineWidth(4);
					pEv.gc.drawRectangle(2, 2, boundingBox.width - 4, boundingBox.height - 4);
					pEv.gc.setForeground(backup);
					pEv.gc.setLineWidth(backupLineWidth);
				}
	
			}}); 
	}
	
	/**
	 * @wbp.parser.entryPoint
	 */
	public Shell createContents() {
		
		imShell = new Shell();
		imShell.addMouseWheelListener(new MouseWheelListener() {
			public void mouseScrolled(MouseEvent arg0) {
				// When the mouse is scrolled in the tool area, modify the zoom percentage.
				zoomSpinner.setSelection(zoomSpinner.getSelection() + arg0.count);
			}
		});
		
		imShell.getDisplay().addFilter(SWT.KeyDown, new Listener() {
			@Override
			public void handleEvent(Event event) {
				switch (event.keyCode) {
				case SWT.PAGE_DOWN:
				case SWT.ARROW_DOWN:
					moveTurn(1);
					break;
				case SWT.PAGE_UP: 
				case SWT.ARROW_UP:
					moveTurn(-1);
					break;
				default:
					break;
					
				}
				
				char c = event.character;
                System.out.println("Pressed: " + c);
				
			}
		});
		
		imShell.setImage(LaunchPad.getIcon());
		// imShell.setSize(800, 540);
		imShell.setText("Initiative Manager");
		imShell.addShellListener(new ShellListener(){

			@Override
			public void shellActivated(ShellEvent e) {
				// no-op
				
			}

			@Override
			public void shellClosed(ShellEvent e) {
				persistContent(null);  // Save state to default save file on close.
				
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
		
		// Clean up after ourselves...
		imShell.addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(DisposeEvent e) {
				SWTResourceManager.disposeAll();
			}
		});
		
		viewPort = new ScrolledComposite( imShell, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER );
		viewPort.setBounds(0, CONTROLBAR_HEIGHT, imShell.getBounds().width - SROLLBAR_WIDTH, imShell.getBounds().height);
		viewPort.setExpandHorizontal( true );
		viewPort.setExpandVertical( true );
		viewPort.getVerticalBar().addSelectionListener( new SelectionAdapter() {
			  public void widgetSelected( SelectionEvent event ) {
				   manageSizing();
				  }});
		
		
		createCharacterWindow();
		
		viewPort.setContent(characterWindow);
		
		viewPort.setMinSize(getChildrenMaxLocation());
	
		
		imShell.addListener( SWT.Resize, event -> {
			manageSizing();
			} );
		
		createToolbarFunctions();
		
		createTurnManagement();
		
		createMenuBar();
		
		createTavern();
		
		readContent(null); // defaults to the last auto-saved file.
		
		return imShell;
	}
	
	
	/**
	 * Clear any previously rolled stat values from all cards
	 */
	protected void clearRollValues() {
		for (InitiativeDisplayGroup idg : idgList) {
			idg.setRollValue(0);
		}

	}
	
	/**
	 * Cycles through the control list and compares it to the turn index to 
	 * ensure that each card knows whether or not it is their turn.
	 */
	protected void verifyWhoseTurnItIs() {
		if (idgList.size() > turnIndex) {
			Control turnCtl = idgList.get(turnIndex).getControl();
			for (InitiativeDisplayGroup idg : idgList) {
				boolean lastTurnState = idg.isMyTurn();
				
				// Tell each card if it is their turn or not.
				if (idg.getControl().equals(turnCtl)) {
					idg.setMyTurn(true);
				} else {
					idg.setMyTurn(false);
				}
				
				if (lastTurnState != idg.isMyTurn()) {
					idg.getControl().redraw();
				}
			}
		}
	}
	
	/**
	 * Advance the initiative turn marker by the number of people indicated by index.
	 */
	protected void moveTurn(int index) {
		turnIndex = turnIndex + index;
		
		// Make sure the turn marker "wraps around" at the ends of the list.
		if (turnIndex < 0) {
			turnIndex = idgList.size() - 1;
		} else if (turnIndex > idgList.size() - 1) {
			turnIndex = 0;
		}
		
		clearRollValues();
		
		// In skinnyView things need to be resized on "prev/next turn" action because
		// the current initiative card will be expanded.
		if (skinnyView) {
			straightenCards();
			verifyWhoseTurnItIs();
		} else {
			verifyWhoseTurnItIs();
			manageSizing();
		}
		
	}
	
	/**
	 * Sets up controls for iterating through turns.
	 */
	protected void createTurnManagement() {
		Button prevTurn = new Button(imShell, SWT.NONE);
		prevTurn.setBounds(280, 14, 60, 20);
		prevTurn.setText("Prev.");	
		
		Button nextTurn = new Button(imShell, SWT.NONE);
		nextTurn.setBounds(345, 14, 60, 20);
		nextTurn.setText("Next");		
		
		// Iterate through the turns (backward)
		prevTurn.addMouseListener(new MouseListener() {

			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}

			@Override
			public void mouseDown(MouseEvent e) {
				moveTurn(-1);
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
				moveTurn(1);
			}

			@Override
			public void mouseUp(MouseEvent e) {
			}

		});			
		
		
		// TODO: Make this configurable.
		String turnMarkerFile = "turnArrow.png";
		turnArrow = SWTResourceManager.createImageResource(characterWindow, turnMarkerFile);
	}
	
	/**
	 * Set up the character window, including the paint listener for that window.
	 */
	protected void createCharacterWindow() {
		characterWindow = new Composite(viewPort, SWT.NONE);
		
		// Draw the turn arrows.
		characterWindow.addPaintListener(new PaintListener() {

			/**
			 * Take care of any painting that needs to be done at the card level.
			 * 
			 * @param pEv
			 */
			protected void paintCardDecorations(PaintEvent pEv) {
				for (InitiativeDisplayGroup idg : idgList) {	
					// log.debug("Painting card decorations.");
					
					// If the player is in a "ready" state, draw the ready icon.
					if (idg.isReadied() && idg.getControl().isVisible()) {
						int readySize = (int) (InitiativeManager.READY_INSET * scale);
						if (readyPicture == null) {
							readyPicture = SWTResourceManager.createImageResource(characterWindow, "ready.png");
						}

						if (readyPicture != null) {
							pEv.gc.drawImage(readyPicture,
									/* image source dimensions */ 		0, 0, readyPicture.getBounds().width, readyPicture.getBounds().height,
									/* image destination dimensions */	idg.getControl().getBounds().x - readySize, idg.getControl().getBounds().y, readySize, readySize
									);
						} else {
							log.error("No graphic found for the ready picture - you may need to re-build...");
							pEv.gc.drawText("Ready!", idg.getControl().getBounds().x - readySize, idg.getControl().getBounds().y);
							pEv.gc.drawText("(no graphic)", idg.getControl().getBounds().x - readySize, idg.getControl().getBounds().y + 12);
						}
					}
					
					// Draw the roll values, if applicable.
					if (idg.getRollValue() > 0) {
						StringBuffer rollValue = new StringBuffer();
						rollValue.append(idg.getRollValue());
						rollValue.append(" + ");
						String value = idg.getAttributeValue(cmbRollChoice.getText());
						rollValue.append(value);
						
						try {
							Integer numericvalue = new Integer(value);
							int total = idg.getRollValue() + numericvalue;
							rollValue.append(" = ");
							rollValue.append(total);
						} catch (NumberFormatException nfe) {
							rollValue.append(" = ?");
						}
						
						FontData[] fD = idg.getControl().getFont().getFontData();
						if (skinnyView) {
							fD[0].setHeight((int) CARD_SKINNY_HEIGHT - 5);
						} else {
							fD[0].setHeight((int) (ROLL_FONT_HEIGHT * scale));
						}
						
						SWTResourceManager.releaseFontResource(pEv.gc.getFont());
						pEv.gc.setFont(SWTResourceManager.createFontResource(idg.getControl(),fD[0]));		
						
						pEv.gc.drawText(rollValue.toString(), idg.getControl().getBounds().x + idg.getControl().getBounds().width, idg.getControl().getBounds().y + 5);
					}
				}
			}
			
			/**
			 * Draws the tavern-related graphics.
			 * 
			 * @param pEv
			 */
			protected void paintTavern(PaintEvent pEv) {
				// Position the tavern on the screen.
				if (tavern != null && !tavern.isDisposed()) {
					tavern.setBounds(viewPort.getBounds().width - (int)(190) - SROLLBAR_WIDTH, viewPort.getOrigin().y, (int)(190), (int)(150));  // TODO: Decide on tavern scaling.
					tavern.redraw();
				}
			}
			
			@Override
			public void paintControl(PaintEvent pEv) {
				// If a card has been deleted out of the initiative list, the
				// arrow index must be reset to be no further along than the last card.
				if (turnIndex >= idgList.size()) {
					turnIndex = idgList.size() - 1;
				}
				
				// If we have an empty list, don't try to paint an initiative arrow.
				if (idgList.size() != 0) {
					
					// The first card added to a list will automatically be the one with the active turn.
					if (turnIndex < 0) {
						turnIndex = 0;
					}
					
					Control turnCtl = idgList.get(turnIndex).getControl();
					
					// Force the card whose turn it is to be visible within the viewport
					viewPort.showControl(turnCtl);
					
					Rectangle idgSpot = turnCtl.getBounds();
					
					if (getTurnArrow() != null) {
						// If our turn arrow graphic is set, draw the graphic.
						pEv.gc.drawImage(getTurnArrow(),0,idgSpot.y + 10);
					} else {
						// Otherwise, log an error and draw a green circle instead.
						log.error("No turn arrow graphic is set.");
						Color backup = pEv.gc.getForeground();
						pEv.gc.setForeground(pEv.display.getSystemColor(SWT.COLOR_GREEN));
						pEv.gc.drawOval(10,10,0,idgSpot.y + 10);
						pEv.gc.setForeground(backup);
					}
					
					// Cycle through the cards and draw any decorations that are outside the cards (but associated with them)
					paintCardDecorations(pEv);
					
					// Paint tavern-related graphics
					paintTavern(pEv);
				}
			}}); 
		
		// characterWindow.setBackgroundImage(SWTResourceManager.createImageResource(characterWindow, "patraeltile.png"));
	}
	
	/**
	 * Set up the menu bar.
	 */
	protected void createMenuBar() {
		Menu menu = new Menu(imShell, SWT.BAR);
		imShell.setMenuBar(menu);
		
		MenuItem miFile = new MenuItem(menu, SWT.CASCADE);
		miFile.setText("&File");
		
		Menu menuFile = new Menu(miFile);
		miFile.setMenu(menuFile);
		
		MenuItem mntmload = new MenuItem(menuFile, SWT.NONE);
		mntmload.setText("&Load");
		mntmload.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				FileDialog loadFile = new FileDialog(imShell, SWT.OPEN);
				String[] filterExtensions = {"*.json"};
				loadFile.setFilterExtensions(filterExtensions);
				loadFile.open();
				String fileName = loadFile.getFileName();
				if (fileName != null && fileName.length() > 0) {
					readContent(fileName);
				}
			}
		});
		
		MenuItem mntmsave = new MenuItem(menuFile, SWT.NONE);
		mntmsave.setText("&Save");
		mntmsave.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				FileDialog loadFile = new FileDialog(imShell, SWT.SAVE);
				String[] filterExtensions = {"*.json"};
				loadFile.setFilterExtensions(filterExtensions);
				loadFile.open();
				String fileName = loadFile.getFileName();
				if (fileName != null && fileName.length() > 0) {
					persistContent(fileName);
				}
			}
		});
	}
	
	/**
	 * Set up the options in the roller combo box with the possible attributes from all character cards.
	 */
	protected void populateRollerCombo() {
		String currentText = cmbRollChoice.getText();
		ArrayList<String> comboChoices = new ArrayList<String>();
		// Reset the available choices in the roller combo...
		for (InitiativeDisplayGroup idg : idgList) {
			for (ValueLabel vl : idg.getAttributes()) {
				if (!comboChoices.contains(vl.getName())) {
					comboChoices.add(vl.getName());
				}
			}
		}
		
		cmbRollChoice.removeAll();
		comboChoices.sort(new Comparator<String>() {

			@Override
			public int compare(String obj1, String obj2) {
			    if (obj1 == null) {
			        return -1;
			    }
			    if (obj2 == null) {
			        return 1;
			    }
			    if (obj1.equals( obj2 )) {
			        return 0;
			    }
			    return obj1.compareTo(obj2);
			}});

		for (String choice : comboChoices) {
			cmbRollChoice.add(choice);
		}
		cmbRollChoice.setText(currentText);
	}

	/**
	 * Add a new character card to the list.
	 * @param name
	 * @param arrayList 
	 * @return
	 */
	protected InitiativeDisplayGroup addCharacterCard(String name, ArrayList<ValueLabel> attributeList, boolean tavern) {
		log.debug("Adding card: " + name);
		int listsize = controlMap.size();
		int xPos = ANCHOR_X;
		int yPos = (int) (ANCHOR_Y + (getCardHeight() * listsize * scale));
		InitiativeDisplayGroup characterInitiativeCard = new InitiativeDisplayGroup(name, getInitiativeManager());
		characterInitiativeCard.setBounds(xPos, yPos, (int)(CARD_WIDTH * scale), (int)(getCardHeight() * scale));
		
		if (tavern) {
			tavernList.add(characterInitiativeCard);
			characterInitiativeCard.getControl().setVisible(false);
		} else {
			controlMap.put(characterInitiativeCard.getControl(), characterInitiativeCard);
			idgList.add(characterInitiativeCard);
		}
		
		if (attributeList == null) {
			characterInitiativeCard.loadDefaultAttributes();
		} else {
	    	for (ValueLabel vl : attributeList) {
	    		characterInitiativeCard.addAttribute(characterInitiativeCard, vl.getName(), vl.getValue());
	    	}
		}
		
		scaleControl(characterInitiativeCard.getControl(), scale);
		characterInitiativeCard.requestLayout(scale);	
		
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
	 * Given a character card, remove it from the initiative order and place them in the tavern.
	 * 
	 * @param initiativeDisplayGroup
	 */
	public void moveToTavern(InitiativeDisplayGroup initiativeDisplayGroup) {
		controlMap.remove(initiativeDisplayGroup.getControl());
		idgList.remove(initiativeDisplayGroup);
		tavernList.add(initiativeDisplayGroup);
		this.syncTavernMenu();
		manageSizing();
	}
	
	/**
	 * Given a character card, remove it from the tavern and put them at the bottom of the initiative order.
	 * 
	 * @param initiativeDisplayGroup
	 */
	public void moveFromTavern(String cardToGetFromTavern) {
		InitiativeDisplayGroup cardToMove = null;
		
		for (InitiativeDisplayGroup idg : tavernList) {
			if (idg.getCharacter().equals(cardToGetFromTavern)) {
				cardToMove = idg;
				break;
			}
		}
		
		if (cardToMove != null) {
			controlMap.put(cardToMove.getControl(), cardToMove);
			idgList.add(cardToMove);
			cardToMove.getControl().setVisible(true);
			tavernList.remove(cardToMove);
			this.syncTavernMenu();
			manageSizing();
		}
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
	 * @return the window which contains the character cards.
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
					// log.debug("childEventHandler: " + source);
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
	public ArrayList<InitiativeDisplayGroup> getInitiativeCards() {
		return idgList;
	}
	
	/**
	 * @return
	 */
	public ArrayList<InitiativeDisplayGroup> getTavernCards() {
		return tavernList;
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
		c.setBounds(c.getBounds().x, c.getBounds().y, (int)(CARD_WIDTH * scale), (int)(getCardHeight() * zoomScale));
		FontData[] fD = c.getFont().getFontData();
		fD[0].setHeight((int) (FONT_HEIGHT * zoomScale));
		

		SWTResourceManager.releaseFontResource(c.getFont());
		c.setFont(SWTResourceManager.createFontResource(c,fD[0]));		     	
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
				if (c instanceof Group) {
					scaleControl(c, scale);
				}
			}	
			
			priorScale = scale;
		}
		
		// determine how much room the character cards take up (max x and y)
		Point pt = getChildrenMaxLocation();
		
		// Set the viewport to the size of the parent shell.
		viewPort.setBounds(0, CONTROLBAR_HEIGHT, imShell.getBounds().width - SROLLBAR_WIDTH, (imShell.getBounds().height - CONTROLBAR_HEIGHT));
		
		// Set the minimum size so the viewport can decide if scrolling needs to be allowed.
		viewPort.setMinSize(pt);	
		
		straightenCards();
		windowLocation.setFromRectangle(imShell.getBounds());
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
		int xPos = InitiativeManager.ANCHOR_X;
		int yPos = InitiativeManager.ANCHOR_Y;
		
		for (InitiativeDisplayGroup idg : idgList) {
			Point location = new Point(xPos, yPos);
			yPos = yPos + (int) idg.doSizing(location, i, skinnyView);
			idg.setUiState(InitiativeDisplayGroup.UI_STATE_NORMAL);
			i++;
		}
		
		verifyWhoseTurnItIs();
	    characterWindow.redraw();
	    
	}
	
	/**
	 * Saves the current configuration of the initiative board to a JSON file.
	 */
	public void persistContent(String filename) {
		
		if (filename == null)  {
			filename = "Output.json";
		}
		
		try (Writer writer = new FileWriter(filename)) {
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
	 * @param filename
	 */
	public void readContent(String filename) {
		loadStatuses();
		
		if (filename == null)  {
			filename = "Output.json";
		}
		
		idgList.clear();
		controlMap.clear();
		for (Control c : characterWindow.getChildren()) {
			if (c instanceof Group) {
				c.setVisible(false);
				c.dispose();
			}
		}
		
		try (Reader reader = new FileReader(filename)) {
		    final GsonBuilder builder = new GsonBuilder();
		    builder.excludeFieldsWithoutExposeAnnotation();
		    final Gson gson = builder.create();
		    // gson.toJson(persistMap, writer);
		    InitiativeManager dummyManager = gson.fromJson(reader, InitiativeManager.class);
		    this.scale = dummyManager.scale;
		    this.turnIndex = dummyManager.turnIndex;
		    if (dummyManager.windowLocation != null && dummyManager.windowLocation.isValid()) {
		    	imShell.setBounds(dummyManager.windowLocation.x, dummyManager.windowLocation.y, 
		    			dummyManager.windowLocation.width, dummyManager.windowLocation.height);
		    } else {
		    	imShell.setSize(800, 640);
		    }
		    
		    zoomSpinner.setSelection((int)(scale * 100));
		    
		    for (InitiativeDisplayGroup dummyGroup : dummyManager.getInitiativeCards()) {
		    	InitiativeDisplayGroup idg = this.addCharacterCard(dummyGroup.getCharacter(), dummyGroup.getAttributes(), false);
		    	idg.setReadied(dummyGroup.isReadied());
		    	
		    	if (dummyGroup.statuses != null) {
			    	for (String status : dummyGroup.statuses.keySet()) {
			    		idg.toggleStatus(statusMetadata.get(status));
			    	}
			    	idg.syncStatusMenuState();
		    	}
		    	
		    	idg.requestLayout(scale);
		    }
		    
		    for (InitiativeDisplayGroup dummyGroup : dummyManager.getTavernCards()) {
		    	InitiativeDisplayGroup idg = this.addCharacterCard(dummyGroup.getCharacter(), dummyGroup.getAttributes(), true);
		    	idg.setReadied(dummyGroup.isReadied());
		    	
		    	if (dummyGroup.statuses != null) {
			    	for (String status : dummyGroup.statuses.keySet()) {
			    		idg.toggleStatus(statusMetadata.get(status));
			    	}
			    	idg.syncStatusMenuState();
		    	}
		    	
		    	idg.requestLayout(scale);
		    }
		    syncTavernMenu();
		    manageSizing();
		    populateRollerCombo();
		} catch (JsonSyntaxException jse) {
			// TODO: Display error messages to the user when appropriate.
			log.error(GameException.fullExceptionInfo(jse));
		} catch (IOException ie) {
			// TODO: Display error messages to the user when appropriate.
			log.error(GameException.fullExceptionInfo(ie));
		}
	}

	/**
	 * Set up the status metadata.
	 */
	public void loadStatuses() {
		// Load attributes for this initiative card from the default file.

		try {
			// TODO: Make default property file name configurable.
			Scanner statusFile = new Scanner(LaunchPad.class.getResourceAsStream("/com/deathfrog/utils/DefaultAttributes.json"));
			if (statusFile != null) {
				JsonArray ja = JsonUtils.readJsonStream(statusFile, "statuses");
				log.info(ja);
				for (JsonElement je : ja) {
					log.info(je);
					if (je.isJsonObject()) {
						StatusMetadata statMeta = new StatusMetadata();
						statMeta.setColor(JsonUtils.getJsonString(je, "color"));
						statMeta.setName(JsonUtils.getJsonString(je, "name"));
						statMeta.setIconName(JsonUtils.getJsonString(je, "icon"));
						statMeta.setDescription(JsonUtils.getJsonString(je, "description"));
						statusMetadata.put(statMeta.getName(), statMeta);
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
}
