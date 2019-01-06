package com.deathfrog.ui.initiative;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

/**
 * Class which supports a second duplicate display of the initiative manager information for a second monitor
 * while still allowing the functionality of the master on the first monitor.
 * 
 * TODO: Reflect changes upon name updates, attribute edits or status effects
 * TODO: Prevent dragging and tavern context menus from the mirror.
 * 
 * @author Al Mele
 *
 */
public class MirroredInitiativeManager extends InitiativeManager {
	protected InitiativeManager parent = null;
	
	public MirroredInitiativeManager() {
		super();
	}
	
	
	/**
	 * @param newParent
	 */
	public void setParent(InitiativeManager newParent) {
		log.debug("Parent of mirror set.");
		parent = newParent;
		statusMetadata = parent.statusMetadata;  // Copy the status metadata from the parent.
	}
	
	@Override
	public Shell createContents() {
		Shell imShell = super.createContents();
		
		imShell.setText("Mirror: " + imShell.getText());
		
		Menu menubar = imShell.getMenuBar();
		
		// Turn off the file menu and the Mirror menu under options.
		for(int i = 0; i < menubar.getItems().length ; i++) {
			MenuItem mi = menubar.getItem(i);
			if (mi.getText().equals("&File")) {
				mi.setEnabled(false);
			}
			if (mi.getText().equals("&Options")) {
				for(int j = 0; j < mi.getMenu().getItems().length ; j++) {
					MenuItem sub = mi.getMenu().getItem(j);
					
					if (sub.getText().equals("&Mirror")) {
						sub.setEnabled(false);
					}
				}
			}
		}
		
		// Disable all the buttons in the mirror.
		for (int i = 0; i < imShell.getChildren().length ; i++) {
			Control c = imShell.getChildren()[i];
			if (c instanceof Button) {
				c.setEnabled(false);
			}
		}
		
		return imShell;
	}
	
	/**
	 * @param mirroredCard
	 * @param sourceCard
	 * @return true if the two cards differ and require synchronization, otherwise false.
	 */
	protected boolean compareMirrorToSource(InitiativeDisplayGroup mirroredCard, InitiativeDisplayGroup sourceCard) {
		boolean needsSync = false;
		
		if (!mirroredCard.getCharacter().equals(sourceCard.getCharacter())) {
			log.debug("Card names don't match - need sync.");
			needsSync = true;
		}
		
		// If any attribute values differ, indicate the need to re-sync.
		for (ValueLabel vl : sourceCard.getAttributes()) {
			if (!vl.getValue().equals(mirroredCard.getAttributeValue(vl.getName()))) {
				log.debug("Card attributes don't match - need sync. Source: " + vl.getName() + " '" + vl.getValue() + "' Mirror Value: '" + mirroredCard.getAttributeValue(vl.getName()) + "'");
				needsSync = true;
			}
		}
		
		if (!needsSync) {
			// If the source card has statuses, make sure the mirror card gets them.
	    	if (sourceCard.statuses != null) {
		    	for (String status : sourceCard.statuses.keySet()) {
		    		if (!mirroredCard.statuses.containsKey(status)) {
		    			needsSync = true;
		    		}
		    	}
	    	}
	    	
	    	// If the mirror card has statuses, make sure they still need them 
	    	if (mirroredCard.statuses != null) {
		    	for (String status : mirroredCard.statuses.keySet()) {
		    		if (!sourceCard.statuses.containsKey(status)) {
		    			needsSync = true;
		    		}
		    	}
	    	}
		}
		
		return needsSync;
	}
	
	/**
	 * @param original
	 */
	protected void reflect(boolean forceMirrorRefresh) {
		log.debug("Mirror reflection.");
		
		if (imShell != null && !imShell.isDisposed()) {
			log.debug("Synchronizing mirror to master.");
			
			boolean needsSync = forceMirrorRefresh;
			
			if (idgList.isEmpty() 
					|| (idgList.size() != parent.idgList.size()) 
					|| (tavernList.size() != parent.tavernList.size()) 
			) {
				log.debug("List sizes don't match.");
				needsSync = true;
			}

			if (!needsSync) {
				for (int i = 0; i < parent.idgList.size(); i++) {
					InitiativeDisplayGroup mirroredCard = idgList.get(i);
					InitiativeDisplayGroup sourceCard = parent.idgList.get(i);
					
					if (compareMirrorToSource(mirroredCard, sourceCard)) {
						needsSync = true;
					} else {
						// Synchronize the ready state and all statuses to the source card.
						mirroredCard.setReadied(sourceCard.isReadied());				    	
					}
				}
				for (int i = 0; i < parent.tavernList.size(); i++) {
					InitiativeDisplayGroup mirroredCard = tavernList.get(i);
					InitiativeDisplayGroup sourceCard = parent.tavernList.get(i);
					
					if (compareMirrorToSource(mirroredCard, sourceCard)) {
						needsSync = true;
					}
				}
			}
			
			if (needsSync) {
				this.clearContent();
				cloneCharacterCards(parent);
				
				for (InitiativeDisplayGroup idg : idgList) {
					log.debug("Removing context menu form mirrored character cards.");
					idg.setContextMenu(null);
				}
			}
			
			this.turnIndex = parent.turnIndex;
			manageSizing(false);
		} else {
			log.error("Attempted to reflect the initiatve window when the shell was null or not created.");
		}
	}
	
	@Override
	public void persistContent(String filename) {
		// NO-OP.  The mirror should never cause any persistence.
	}
	
	@Override
	public void readContent(String filename) {
		// The mirror should not do the normal persistence activities of the parent window.
		
		// Just set the bounds to the parent with an offset.
		imShell.setBounds(parent.windowLocation.x + 10, parent.windowLocation.y - 10, 
				parent.windowLocation.width, parent.windowLocation.height);
	}
	
	@Override
	public void close() {
		if (parent.isOpen()) {
			parent.mntmMirror.setSelection(false);
		}
		super.close();
		log.debug("Closed mirror window.");
	}
	
	@Override
	public void open() {
		log.debug("Mirror opening requested!");
		Display display = Display.getDefault();
		imShell = createContents();
		
		imShell.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				close();	
				
			}
		});
		
		imShell.open();
		imShell.layout();
		
		reflect(true);
		
		while (!imShell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}
}
