package com.deathfrog.utils.ui;



import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;



/**
 * @author Al Mele
 *
 */
public class RollerInterface {
	protected static Logger log = LogManager.getLogger(RollerInterface.class);
	public static final int COL1_X = 20;
	public static final int COL2_X = 100;
	public static final int COL3_X = 160;
	public static final int COL4_X = 220;
	public static final int COL5_X = 310;

	static Shell riShell = null;
	
	
	/**
	 * @return
	 */
	public Shell createContents() { 
		Shell shell = new Shell();
		shell.setImage(LaunchPad.getIcon());
		shell.setSize(560, 311);
		shell.setText("Roll Those Dice!");
		
		// Configure the header text
		Label lblDieSides = new Label(shell, SWT.NONE);
		lblDieSides.setBounds(COL2_X, 38, 55, 15);
		lblDieSides.setText("Die Sides");
		
		Label lblNumberOfDice = new Label(shell, SWT.NONE);
		lblNumberOfDice.setBounds(COL3_X, 38, 55, 15);
		lblNumberOfDice.setText("# of Dice");
		
		Label lblMin = new Label(shell, SWT.NONE);
		lblMin.setText("Min");
		lblMin.setBounds(COL4_X, 38, 55, 15);
		
		Label lblResult = new Label(shell, SWT.NONE);
		lblResult.setBounds(COL5_X, 38, 55, 15);
		lblResult.setText("Result");
		
		// Configure the first row of dice settings
		DynamicInputAddition.addRow(shell);
		
		// Allow subsequent dice rolling settings to be added dynamically.
		Button btnAdd = new Button(shell, SWT.NONE);
		btnAdd.addSelectionListener(new DynamicInputAddition(shell));
		btnAdd.setBounds(349, 7, 75, 25);
		btnAdd.setText("Add");
		
		return shell;
	}
	
	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		riShell = createContents();
		riShell.open();
		riShell.layout();
		while (!riShell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}
	
	/**
	 * 
	 */
	public void close() {
		if (riShell != null && !riShell.isDisposed()) {
			riShell.close();
		}
	}
	
	public Shell getShell() {
		return riShell;
	}
	
	public boolean isOpen() {
		return (riShell != null && !riShell.isDisposed());
	}
}
