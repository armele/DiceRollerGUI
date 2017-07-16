package com.deathfrog.utils.ui;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;



/**
 * @author Al Mele
 *
 */
public class InputSet implements ModifyListener {
	protected Spinner diceSides = null;
	protected Spinner diceNumber = null;
	protected Spinner diceMin = null;
	
	protected Text resultLabel = null;
	
	public Spinner getDiceSides() {
		return diceSides;
	}
	public void setDiceSides(Spinner diceSides) {
		this.diceSides = diceSides;
	}
	public Spinner getDiceNumber() {
		return diceNumber;
	}
	public void setDiceNumber(Spinner diceNumber) {
		this.diceNumber = diceNumber;
	}
	public Text getResultLabel() {
		return resultLabel;
	}
	public void setResultLabel(Text resultLabel) {
		this.resultLabel = resultLabel;
	}
	/**
	 * @return the diceMin
	 */
	public Spinner getDiceMin() {
		return diceMin;
	}
	
	/**
	 * @param diceMin the diceMin to set
	 */
	public void setDiceMin(Spinner diceMin) {
		this.diceMin = diceMin;
	}
	
	/* (non-Javadoc)
	 * Enforce data integrity relationships between fields on the row.  
	 * - Ensure the minimum die value is less than or equal to the number of sides on the die
	 * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
	 */
	@Override
	public void modifyText(ModifyEvent arg0) {
		if (diceMin.getText().length() > 0) {
			Integer minVal = new Integer(diceMin.getText());
			Integer sides = new Integer(diceSides.getText());
			
			if (minVal > sides) {
				diceMin.setSelection(sides.intValue());
			}
		}
	}
	
	
}
