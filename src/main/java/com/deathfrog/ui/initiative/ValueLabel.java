package com.deathfrog.ui.initiative;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;

import com.deathfrog.utils.ui.SWTResourceManager;
import com.google.gson.annotations.Expose;

public class ValueLabel {
	protected static Logger log = LogManager.getLogger(ValueLabel.class);
	private static final int LBL_PAD = 5;
	private static final int TXT_MINEDITSIZE = 25;
	static final int LBL_FONT_HEIGHT = 8;
	
	protected Font fontCreatedForScaling = null;  // Track for disposal
	protected InitiativeDisplayGroup initiativeCard = null;
	protected Composite valueLabelControl = null;
	
	@Expose(serialize = true, deserialize = true)
	protected String name;
	protected Text txtName;
	protected Label lblName;
	
	@Expose(serialize = true, deserialize = true)
	protected String value; 
	protected Label lblValue;
	
	protected Text txtValue;
	protected double currentScale = 1.0;
	
	/**
	 * @param parent
	 * @param text
	 * @param value
	 */
	public ValueLabel(InitiativeDisplayGroup parent, String attr, String val) {
		initiativeCard = parent;
		name = attr;
		value = val;
		
		// A value label is made up of a label component and two forms of value component (non-editable value
		// and editable value.  Only one of the editable/non-editable pair of controls will be visible at the same time.
		valueLabelControl = new Composite((Composite)initiativeCard.getControl(), SWT.NONE);
		valueLabelControl.addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(DisposeEvent e) {
				txtName.dispose();
				lblName.dispose();
				lblValue.dispose();
				txtValue.dispose();
			}
		}); 
		
		configureLabels();
		configureTextboxes();
        
        positionControls(1.0);
	}
	
	/**
	 * Configure the controls that are for displaying information (not editing)
	 */
	protected void configureLabels() {
		ValueLabel me = this;  // Workaround for anonymous classes not being able to reference "this" within it.
		// Set up name controls.
		lblName = new Label(valueLabelControl, SWT.NONE);
		lblName.setText(name);

        
        // If the user clicks an attribute label, give them edit ability for the attribute value.
		lblName.addMouseListener(new MouseListener() {

			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}

			@Override
			public void mouseDown(MouseEvent e) {
				if (initiativeCard.getUiState() == InitiativeDisplayGroup.UI_STATE_NORMAL && e.button == InitiativeDisplayGroup.LEFT_BUTTON) {
					log.debug("Edit mode enabled from label.");
					initiativeCard.attributeEditEvent(me);
					editValue(true);
				} else if (initiativeCard.getUiState() == InitiativeDisplayGroup.UI_STATE_NORMAL && e.button == InitiativeDisplayGroup.RIGHT_BUTTON) {
					initiativeCard.setSuppressContextMenu(true);
					initiativeCard.attributeEditEvent(me);
					editName(true);
				}
			}

			@Override
			public void mouseUp(MouseEvent e) {
			}

		});	  
        
        
        lblValue = new Label(valueLabelControl, SWT.NONE);
        lblValue.setText(value);
        
        // If the user clicks an attribute value, give them edit ability for that value.        
        lblValue.addMouseListener(new MouseListener() {

			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}

			@Override
			public void mouseDown(MouseEvent e) {
				if (initiativeCard.getUiState() == InitiativeDisplayGroup.UI_STATE_NORMAL && e.button == InitiativeDisplayGroup.LEFT_BUTTON) {
					log.debug("Edit mode enabled from value.");
					initiativeCard.attributeEditEvent(me);
					editValue(true);
				}
			}

			@Override
			public void mouseUp(MouseEvent e) {
			}

		});	    		
	}
	
	/**
	 * Configure the controls that are for data entry.
	 */
	protected void configureTextboxes() {
		ValueLabel me = this;  // Workaround for anonymous classes not being able to reference "this" within it.
        txtName = new Text(valueLabelControl, SWT.NONE);
        txtName.setText(name);
        txtName.setVisible(false);
        // When the user is finished typing an attribute name in, end the editing function by hiding the entry box and re-showing the label.
        txtName.addTraverseListener(new TraverseListener() {

			@Override
			public void keyTraversed(TraverseEvent e) {
				if (e.getSource().equals(txtName)){
					log.debug("Ending name edit mode - Return.");
					editName(false);
					editValue(true);
				}
			}


		});	   
        
        txtValue = new Text(valueLabelControl, SWT.NONE);
        txtValue.setText(value);
        txtValue.setVisible(false);
        
        // When the user is finished typing an attribute value in, end the editing function by hiding the entry box and re-showing the label.
        txtValue.addTraverseListener(new TraverseListener() {

			@Override
			public void keyTraversed(TraverseEvent e) {
				if (e.getSource().equals(txtValue) && e.detail == SWT.TRAVERSE_RETURN){
					log.debug("Ending edit mode - Return.");
					editValue(false);
				}
				
				if (e.getSource().equals(txtValue) && e.detail == SWT.TRAVERSE_TAB_NEXT){
					log.debug("Ending edit mode - Tab.");
					editValue(false);
					
					boolean nextAttribute = false;
					for (ValueLabel lbl : initiativeCard.getAttributes()) {
						if (nextAttribute == true) {
							log.debug("Tabbing to next attribute.");
							lbl.editValue(true);
							break;
						}
						if (lbl.equals(me)) {
							nextAttribute = true;
						}
					}
				}
				
			}


		});	    		
	}	
	
	/**
	 * Toggles the edit mode for attribute name.
	 */
	protected void editName(boolean isEdit) {
		// log.debug("Start name edits: " + isEdit);
		
		if (isEdit) {
			lblName.setVisible(false);
			txtName.setVisible(true);
			txtName.setFocus();
			txtName.selectAll();
			txtName.redraw();
		} else {
			name = txtName.getText();
			lblName.setText(name);
			txtName.setVisible(false);
			lblName.setVisible(true);
			positionControls(currentScale);
		}
	}
	
	/**
	 * Toggles the edit mode for attribute values.
	 */
	protected void editValue(boolean isEdit) {
		if (isEdit) {
			lblValue.setVisible(false);
			txtValue.setVisible(true);
			txtValue.setFocus();
			txtValue.selectAll();
			txtValue.redraw();
		} else {
			value = txtValue.getText();
			lblValue.setText(value);
			txtValue.setVisible(false);
			lblValue.setVisible(true);
			positionControls(currentScale);
		}
	}
	
	/**
	 * @param scale
	 */
	public void positionControls(double scale) {
		currentScale = scale;
		FontData[] fD = lblName.getFont().getFontData();
		 // Note that scaling the font and then measuring by the font eliminates the need to scale the controls any other way.
		fD[0].setHeight((int) (LBL_FONT_HEIGHT * scale)); 
		
		SWTResourceManager.releaseFontResource(lblName.getFont());
		lblName.setFont(SWTResourceManager.createFontResource(lblValue,fD[0]));
		
        GC gc = new GC(lblName);
        Point labelSize = gc.textExtent(lblName.getText());
        gc.dispose ();
        
        lblName.setBounds(0, 0, labelSize.x, labelSize.y);
        
		SWTResourceManager.releaseFontResource(txtName.getFont());
		txtName.setFont(SWTResourceManager.createFontResource(lblValue,fD[0]));        
		SWTResourceManager.releaseFontResource(lblValue.getFont());
		lblValue.setFont(SWTResourceManager.createFontResource(lblValue,fD[0]));
		SWTResourceManager.releaseFontResource(txtValue.getFont());
		txtValue.setFont(SWTResourceManager.createFontResource(lblValue,fD[0]));
        
        gc = new GC(lblValue);
        Point valueSize = gc.textExtent(lblValue.getText());
        Point nameSize = gc.textExtent(lblName.getText());
        gc.dispose ();
        
        Rectangle nameTrim = txtName.computeTrim(0, 0, nameSize.x, nameSize.y);
        txtName.setBounds(0, 0, Math.max(nameTrim.width, TXT_MINEDITSIZE), nameTrim.height);
        
        Rectangle editTrim = txtValue.computeTrim(0, 0, valueSize.x, valueSize.y);
        lblValue.setBounds(labelSize.x + LBL_PAD, 0, Math.max(valueSize.x, LBL_PAD), valueSize.y);     
        txtValue.setBounds(labelSize.x + LBL_PAD, 0, Math.max(editTrim.width, TXT_MINEDITSIZE), editTrim.height);
       
        valueLabelControl.requestLayout();
        txtName.requestLayout();
        lblName.requestLayout();
        lblValue.requestLayout();
        txtValue.requestLayout();
	}
	
	/**
	 * @return the labelText
	 */
	public String getName() {
		return name;
	}


	/**
	 * @param labelText the labelText to set
	 */
	public void setAttribute(String labelText) {
		name = labelText;
		lblName.setText(name);
	}

	/**
	 * @return the value associated with this attribute
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param labelValue the labelValue to set
	 */
	public void setValue(String labelValue) {
		value = labelValue;
		lblValue.setText(value);
		txtValue.setText(value);
	}

	/**
	 * 
	 */
	public Composite getValueLabelControl() {
		return valueLabelControl;
	}

	/**
	 * Set up the child controls with a menu
	 * @param contextMenu
	 */
	public void setMenu(Menu contextMenu) {
		lblName.setMenu(contextMenu);
		lblValue.setMenu(contextMenu);
		valueLabelControl.setMenu(contextMenu);
		
		// Inherit the menu listeners of the parent.  This allows the menu suppression logic at the card level to work.
		for (Listener hearThat : initiativeCard.getControl().getListeners(SWT.MenuDetect) ) {
			lblName.addListener(SWT.MenuDetect, hearThat);
			lblValue.addListener(SWT.MenuDetect, hearThat);
			valueLabelControl.addListener(SWT.MenuDetect, hearThat);
		}
		
	}

}
