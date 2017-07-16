package com.deathfrog.utils.ui;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import com.deathfrog.utils.dice.Dice;
import com.deathfrog.utils.dice.Die;

/**
 * @author Al Mele
 *
 */
public class MainRoller extends SelectionAdapter {
	protected InputSet inputSet = null;
	
	public MainRoller(InputSet inputSet) {
		this.inputSet = inputSet;
	}
	
	@Override
	public void widgetSelected(SelectionEvent e) {
		System.out.println("Button pressed.");
		
		processRoll();
		
	}
	
	/**
	 * 
	 */
	public void processRoll() {
		StringBuffer buf = new StringBuffer();
		Dice bag = new Dice();
		Integer sides = new Integer(inputSet.getDiceSides().getText());
		Integer num = new Integer(inputSet.getDiceNumber().getText());
		Integer min = new Integer(inputSet.getDiceMin().getText());
		
		if (min > sides) {
			min = sides;
		}
		
		for (int i = 0; i < num; i++) {
			Die d = new Die(sides.intValue());
			d.setMinValue(min);
			bag.add(d);
			d.roll();
		}
		
		buf.append(new Integer(bag.total(false)).toString());
		
		if (bag.size() > 1) {
			buf.append(" (" + bag.toString() + ")");
		}
		inputSet.getResultLabel().setText(buf.toString());
	}
	
	/**
	 * @return
	 */
	protected static Dice d20() {
		Dice bag = new Dice();
		Die d1 = new Die(20);
		bag.add(d1);
		return bag;
	}

}
