package com.deathfrog.utils.ui;

import org.eclipse.swt.SWT;
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
        
        GC gc = new GC(lblText);
        Point labelSize = gc.textExtent(lblText.getText());
        gc.dispose ();
        
        lblText.setBounds(0, 0, labelSize.x, 24);
        lblValue = new Label(valueLabelControl, SWT.NONE);
        lblValue.setText(value);
        
        gc = new GC(lblText);
        Point valueSize = gc.textExtent(lblValue.getText());
        gc.dispose ();
        
        lblValue.setBounds(labelSize.x + 5, 0, valueSize.x, 24);
        txtValue = new Text(valueLabelControl, SWT.NONE);
        txtValue.setBounds(labelSize.x + 5, 0, valueSize.x, 24);
        txtValue.setText(lblValue.getText());
        txtValue.setVisible(false);
        
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
