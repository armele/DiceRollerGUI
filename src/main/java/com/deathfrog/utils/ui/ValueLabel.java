package com.deathfrog.utils.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class ValueLabel {
	protected Composite valueLabelControl = null;
	protected Label lblText;
	protected Label lblValue;
	protected Text txtValue;
	private static final int LBL_PAD = 5;
	private static final int LBL_FONT_HEIGHT = 10;
	
	/**
	 * @param parent
	 * @param text
	 * @param value
	 */
	public ValueLabel(Composite parent, String text, String value) {
        /* Prototype code for value labels */
		valueLabelControl = new Composite(parent, SWT.NONE);
        lblText = new Label(valueLabelControl, SWT.NONE);
        lblText.setText(text);

        lblValue = new Label(valueLabelControl, SWT.NONE);
        lblValue.setText(value);
        
        txtValue = new Text(valueLabelControl, SWT.NONE);
        txtValue.setText(lblValue.getText());
        txtValue.setVisible(false);
        
        positionControls(1.0);
	}
	
	/**
	 * @param scale
	 */
	public void positionControls(double scale) {
		FontData[] fD = lblText.getFont().getFontData();
		 // Note that scaling the font and then measuring by the font eliminates the need to scale the controls any other way.
		fD[0].setHeight((int) (LBL_FONT_HEIGHT * scale)); 
		lblText.setFont( new Font(lblText.getDisplay(),fD[0]));
		
        GC gc = new GC(lblText);
        Point labelSize = gc.textExtent(lblText.getText());
        gc.dispose ();
        
        lblText.setBounds(0, 0, labelSize.x, labelSize.y);
        
        lblValue.setFont( new Font(lblValue.getDisplay(),fD[0]));
        txtValue.setFont( new Font(txtValue.getDisplay(),fD[0]));
		
        gc = new GC(lblValue);
        Point valueSize = gc.textExtent(lblValue.getText());
        gc.dispose ();
        
        lblValue.setBounds(labelSize.x + LBL_PAD, 0, valueSize.x, valueSize.y);     
        txtValue.setBounds(labelSize.x + LBL_PAD, 0, valueSize.x, valueSize.y);
       
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
