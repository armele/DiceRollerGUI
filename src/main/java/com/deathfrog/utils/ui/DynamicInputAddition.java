package com.deathfrog.utils.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;



/**
 * Dynamically add a row to the dice rolling interface.
 * 
 * @author Al Mele
 *
 */
public class DynamicInputAddition extends SelectionAdapter {
	protected static int increment = 28;
	protected Shell parent = null;
	
	@Override
	public void widgetSelected(SelectionEvent e) {		
		addRow(parent); 
	}
	
	/**
	 * @param parent
	 */
	public DynamicInputAddition(Shell parent) {
		this.parent = parent;
	}
	
	/**
	 * @param parent
	 */
	public static void addRow(Shell parent) {
		Control[] controls = parent.getChildren();
		int yCoord = 0;
		
		for (Control c : controls) {
			if (yCoord < c.getBounds().y) {
				yCoord = c.getBounds().y;
			}
		}
		yCoord = yCoord + increment;

		InputSet inputSetX = new InputSet();
		MainRoller mainRollerX= new MainRoller(inputSetX);
		Button btnRollX = new Button(parent, SWT.NONE);
		btnRollX.addSelectionListener(mainRollerX);
		btnRollX.setBounds(RollerInterface.COL1_X, yCoord-2, 75, 25);
		btnRollX.setText("Roll");		
		
		Spinner sideSpinnerX = new Spinner(parent, SWT.BORDER);
		sideSpinnerX.setBounds(RollerInterface.COL2_X, yCoord, 47, 22);
		sideSpinnerX.setMinimum(2);
		sideSpinnerX.addModifyListener(inputSetX);
		
		Spinner numSpinnerX = new Spinner(parent, SWT.BORDER);
		numSpinnerX.setBounds(RollerInterface.COL3_X, yCoord, 47, 22);
		numSpinnerX.setMinimum(1);
		numSpinnerX.addModifyListener(inputSetX);
		
		Spinner minSpinnerX = new Spinner(parent, SWT.BORDER);
		minSpinnerX.setBounds(RollerInterface.COL4_X, yCoord, 47, 22);
		minSpinnerX.setMinimum(1);
		minSpinnerX.addModifyListener(inputSetX);
		
		Text lblResultX = new Text(parent, SWT.READ_ONLY | SWT.BORDER);
		lblResultX.setEditable(false);		
		lblResultX.setBounds(RollerInterface.COL5_X, yCoord, 200, 22);
		lblResultX.setText("");
		
		inputSetX.setDiceSides(sideSpinnerX);
		inputSetX.setDiceNumber(numSpinnerX);
		inputSetX.setDiceMin(minSpinnerX);
		inputSetX.setResultLabel(lblResultX);		
	}
}
