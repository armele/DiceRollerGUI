package com.deathfrog.utils.ui;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.SWT;
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
import org.eclipse.swt.widgets.Text;

public class ValueLabel {
	protected static Logger log = LogManager.getLogger(ValueLabel.class);
	private static final int LBL_PAD = 5;
	private static final int TXT_MINEDITSIZE = 25;
	private static final int LBL_FONT_HEIGHT = 10;
	
	protected Font fontCreatedForScaling = null;  // Track for disposal
	protected InitiativeDisplayGroup initiativeCard = null;
	protected Composite valueLabelControl = null;
	protected Label lblText;
	protected Label lblValue;
	protected Text txtValue;
	protected double currentScale = 1.0;
	
	/**
	 * @param parent
	 * @param text
	 * @param value
	 */
	public ValueLabel(InitiativeDisplayGroup parent, String text, String value) {
		ValueLabel me = this;  // Workaround for anonymous classes not being able to reference "this" within it.
		initiativeCard = parent;
		
        /* Prototype code for value labels */
		valueLabelControl = new Composite((Composite)initiativeCard.getControl(), SWT.NONE);
        lblText = new Label(valueLabelControl, SWT.NONE);
        lblText.setText(text);
        
        // If the user clicks an attribute label, give them edit ability for the attribute value.
        lblText.addMouseListener(new MouseListener() {

			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}

			@Override
			public void mouseDown(MouseEvent e) {
				log.debug("Edit mode enabled from label.");
				initiativeCard.attributeEditEvent(me);
				editMode(true);
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
				log.debug("Edit mode enabled from value.");
				editMode(true);
			}

			@Override
			public void mouseUp(MouseEvent e) {
			}

		});	        
        
        txtValue = new Text(valueLabelControl, SWT.NONE);
        txtValue.setText(lblValue.getText());
        txtValue.setVisible(false);
        
        // When the user is finished typing an attribute value in, end the editing function by hiding the entry box and re-showing the label.
        txtValue.addTraverseListener(new TraverseListener() {

			@Override
			public void keyTraversed(TraverseEvent e) {
				if (e.getSource().equals(txtValue) && e.detail == SWT.TRAVERSE_RETURN){
					log.debug("Ending edit mode - Keypress.");
					editMode(false);
				}
				
			}


		});	          
        
        positionControls(1.0);
	}
	
	/**
	 * Toggles the edit mode for labels.
	 */
	protected void editMode(boolean isEdit) {
		if (isEdit) {
			lblValue.setVisible(false);
			txtValue.setVisible(true);
			txtValue.setFocus();
			txtValue.redraw();
		} else {
			lblValue.setText(txtValue.getText());
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
		FontData[] fD = lblText.getFont().getFontData();
		 // Note that scaling the font and then measuring by the font eliminates the need to scale the controls any other way.
		fD[0].setHeight((int) (LBL_FONT_HEIGHT * scale)); 
		
		SWTResourceManager.releaseFontResource(lblText.getFont());
		lblText.setFont(SWTResourceManager.createFontResource(lblValue.getDisplay(),fD[0]));
		
        GC gc = new GC(lblText);
        Point labelSize = gc.textExtent(lblText.getText());
        gc.dispose ();
        
        lblText.setBounds(0, 0, labelSize.x, labelSize.y);
        
		SWTResourceManager.releaseFontResource(lblValue.getFont());
		lblValue.setFont(SWTResourceManager.createFontResource(lblValue.getDisplay(),fD[0]));
		SWTResourceManager.releaseFontResource(txtValue.getFont());
		txtValue.setFont(SWTResourceManager.createFontResource(lblValue.getDisplay(),fD[0]));
        
        gc = new GC(lblValue);
        Point valueSize = gc.textExtent(lblValue.getText());
        gc.dispose ();
        
        Rectangle editTrim = txtValue.computeTrim(0, 0, valueSize.x, valueSize.y);
        
        lblValue.setBounds(labelSize.x + LBL_PAD, 0, Math.max(valueSize.x, LBL_PAD), valueSize.y);     
        txtValue.setBounds(labelSize.x + LBL_PAD, 0, Math.max(editTrim.width, TXT_MINEDITSIZE), editTrim.height);
       
        valueLabelControl.requestLayout();
        lblText.requestLayout();
        lblValue.requestLayout();
        txtValue.requestLayout();
	}
	
	/**
	 * @return the labelText
	 */
	public String getText() {
		return lblText.getText();
	}


	/**
	 * @param labelText the labelText to set
	 */
	public void setText(String labelText) {
		lblText.setText(labelText);
	}

	/**
	 * @return the value associated with this attribute
	 */
	public String getValue() {
		return lblValue.getText();
	}

	/**
	 * @param labelValue the labelValue to set
	 */
	public void setValue(String labelValue) {
		lblValue.setText(labelValue);
		txtValue.setText(labelValue);
	}

	/**
	 * 
	 */
	public Composite getValueLabelControl() {
		return valueLabelControl;
	}

}
