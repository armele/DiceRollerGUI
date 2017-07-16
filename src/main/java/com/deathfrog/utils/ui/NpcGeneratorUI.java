package com.deathfrog.utils.ui;


import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.deathfrog.npc.NpcContext;
import com.deathfrog.npc.NpcDefinitions;
import com.deathfrog.npc.NpcPersistor;
import com.deathfrog.utils.GameException;
import com.deathfrog.utils.PercentileList;


public class NpcGeneratorUI {
	protected static Logger log = LogManager.getLogger(NpcGeneratorUI.class);
	
	protected Shell npcGenShell;
	protected static NpcDefinitions npcDef = null;
	protected List<Control> resettableControls = new ArrayList<Control>();
	private Text txtNpcDetails = null;
	private Text txtBuilding;
	
	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {			
			loadDefinitionFile(null);
			NpcGeneratorUI window = new NpcGeneratorUI();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param file
	 */
	protected static void loadDefinitionFile(String file) {
		NpcPersistor npc = new NpcPersistor();
		npcDef = npc.parse(file);		
		npcDef.sort(null);		
	}
	
	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		
		if (npcDef == null) {
			loadDefinitionFile(null);
		}
		
		npcGenShell = createContents();
		npcGenShell.open();
		npcGenShell.layout();
		while (!npcGenShell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * 
	 */
	public void close() {
		if (npcGenShell != null && !npcGenShell.isDisposed()) {
			npcGenShell.close();
		}
	}
	
	public boolean isOpen() {
		return (npcGenShell != null && !npcGenShell.isDisposed());
	}
	
	public Shell getShell() {
		return npcGenShell;
	}
	
	/**
	 * 
	 */
	protected void updateContents() {
		for (Control c : resettableControls) {
			if (c.getClass().isAssignableFrom(Combo.class)) {
				Combo combo = (Combo)c;
				combo.removeAll();
				
				// TODO: Figure out how to distinguish between the different resettable controls.
				for (NpcContext ctx : npcDef) {
					combo.add(ctx.getName());
				}
				combo.select(0);
			}
		}
	}
	
	/**
	 * Create contents of the window.
	 */
	protected Shell createContents() {
		Shell shell = new Shell();
		shell.setImage(LaunchPad.getIcon());
		shell.setSize(450, 300);
		shell.setText("NPC Generator");
		
		Combo combo = new Combo(shell, SWT.READ_ONLY);
		combo.setBounds(70, 12, 124, 23);
		
		for (NpcContext c : npcDef) {
			combo.add(c.getName());
		}
		combo.select(0);
		resettableControls.add(combo);
		
		txtNpcDetails = new Text(shell, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		txtNpcDetails.setEditable(false);
		txtNpcDetails.setBounds(10, 41, 414, 160);
		
		Button btnGenerate = new Button(shell, SWT.NONE);
		btnGenerate.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				NpcContext context = npcDef.get(combo.getSelectionIndex());
				
				if (context != null) {
					txtNpcDetails.setText(context.pickEverything());
					PercentileList<?> buildingList = context.getContextLists().get("Buildings");
					if (buildingList != null) {
						txtBuilding.setText(buildingList.pick().toString());
					}
				} else {
					String errMsg = "No context found for selection " + combo.getSelectionIndex();
					log.error(errMsg);
					throw new GameException(errMsg);
				}
			}
		});
		btnGenerate.setBounds(349, 10, 75, 25);
		btnGenerate.setText("Generate");
		
		Menu menu = new Menu(shell, SWT.BAR);
		shell.setMenuBar(menu);
		
		MenuItem mntmFile = new MenuItem(menu, SWT.CASCADE);
		mntmFile.setText("File");
		
		Menu menuFile = new Menu(mntmFile);
		mntmFile.setMenu(menuFile);
		
		
		MenuItem mntmLoad = new MenuItem(menuFile, SWT.NONE);
		mntmLoad.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
			   FileDialog dialog = new FileDialog(shell, SWT.OPEN);
			   dialog.setFilterExtensions(new String [] {"*.xml"});
			   dialog.setFilterPath(".");
			   String result = dialog.open();
			   
			   if (result != null) {
				   loadDefinitionFile(result);
				   updateContents();
			   }
			}
		});
		
		mntmLoad.setText("Load");
		
		Label lblContext = new Label(shell, SWT.NONE);
		lblContext.setBounds(10, 15, 54, 15);
		lblContext.setText("Context:");
		
		txtBuilding = new Text(shell, SWT.BORDER);
		txtBuilding.setBounds(10, 210, 112, 21);
		
		return shell;
	}
}
