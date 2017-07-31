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

import com.deathfrog.npc.GeneratedNPC;
import com.deathfrog.npc.GenerationConfig;
import com.deathfrog.npc.NpcContext;
import com.deathfrog.npc.NpcDefinitions;
import com.deathfrog.npc.NpcPersistor;
import com.deathfrog.npc.PathfinderClassDefinition;
import com.deathfrog.npc.PathfinderRaceDefinition;
import com.deathfrog.utils.GameException;
import com.deathfrog.utils.PercentileList;


public class NpcGeneratorUI {
	protected static Logger log = LogManager.getLogger(NpcGeneratorUI.class);
	
	protected Shell npcGenShell;
	protected static NpcDefinitions npcDef = null;
	protected List<Control> resettableControls = new ArrayList<Control>();
	private Text txtPattern;
	
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
	@SuppressWarnings("unchecked")
	protected Shell createContents() {
		Shell shell = new Shell();
		shell.setImage(LaunchPad.getIcon());
		shell.setSize(600, 400);
		shell.setText("NPC Generator");
		
		Combo cmbContext = new Combo(shell, SWT.READ_ONLY);
		cmbContext.setBounds(70, 12, 124, 23);
		
		for (NpcContext c : npcDef) {
			cmbContext.add(c.getName());
		}
		cmbContext.select(0);
		resettableControls.add(cmbContext);
		
		// Create and populate a dropdown list of supported classes.
		Combo cmbPfClass = new Combo(shell, SWT.READ_ONLY);
		cmbPfClass.setBounds(70, 41, 124, 23);
		
		NpcContext context = npcDef.get(cmbContext.getSelectionIndex());
		cmbPfClass.add("<Randomize>");
		for (PathfinderClassDefinition pf : (ArrayList<PathfinderClassDefinition>) context.getContextLists().get(NpcContext.CLASS).getItemList()) {
			cmbPfClass.add(pf.getPathfinderClassName());
		}
		
		cmbPfClass.select(0);
		resettableControls.add(cmbPfClass);
		
		// Create and populate a dropdown list of supported races;
		Combo cmbRace = new Combo(shell, SWT.READ_ONLY);
		cmbRace.setBounds(70, 70, 124, 23);
		
		cmbRace.add("<Randomize>");
		for (PathfinderRaceDefinition pf : (ArrayList<PathfinderRaceDefinition>) context.getContextLists().get(NpcContext.RACE).getItemList()) {
			cmbRace.add(pf.getRace());
		}
		
		cmbRace.select(0);		
		resettableControls.add(cmbRace);
		
		Text txtBuilding = new Text(shell, SWT.BORDER);
		
		Text txtNpcDetails = new Text(shell, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		txtNpcDetails.setEditable(false);
		txtNpcDetails.setBounds(10, 144, 386, 160);
		
		Button btnGenerate = new Button(shell, SWT.NONE);
		btnGenerate.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				NpcContext context = npcDef.get(cmbContext.getSelectionIndex());
				
				if (context != null) {
					GenerationConfig gconfig = new GenerationConfig();
					if (cmbPfClass.getSelectionIndex() > 0) {
						gconfig.setFixedClass(cmbPfClass.getText());
					}
					if (cmbRace.getSelectionIndex() > 0) {
						gconfig.setFixedRace(cmbRace.getText());
					}					
					GeneratedNPC npc = context.generateNPC(gconfig);
					txtNpcDetails.setText(npc.toString());
					txtPattern.setText(npc.getName().getPattern());
					PercentileList<?> buildingList = context.getContextLists().get("Buildings");
					if (buildingList != null) {
						txtBuilding.setText(buildingList.pick().toString());
					}
				} else {
					String errMsg = "No context found for selection " + cmbContext.getSelectionIndex();
					log.error(errMsg);
					throw new GameException(errMsg);
				}
			}
		});
		btnGenerate.setBounds(321, 10, 75, 25);
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
		
		txtBuilding.setEditable(false);
		txtBuilding.setBounds(10, 310, 184, 21);
		
		txtPattern = new Text(shell, SWT.BORDER);
		txtPattern.setEditable(false);
		txtPattern.setBounds(462, 144, 112, 21);
		
		Label lblPattern = new Label(shell, SWT.NONE);
		lblPattern.setText("Pattern:");
		lblPattern.setBounds(414, 147, 47, 21);
		
		Label lblPfClass = new Label(shell, SWT.NONE);
		lblPfClass.setText("Class:");
		lblPfClass.setBounds(10, 44, 54, 15);
		
		Label lblRace = new Label(shell, SWT.NONE);
		lblRace.setText("Race:");
		lblRace.setBounds(10, 73, 54, 15);
		
		return shell;
	}
}
