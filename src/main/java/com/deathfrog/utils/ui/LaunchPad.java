package com.deathfrog.utils.ui;

import java.net.URISyntaxException;
import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

import com.deathfrog.ui.initiative.InitiativeManager;
import com.deathfrog.utils.GameException;

/**
 * TODO: Enforce one open window per session and/or set up a panel layout.
 * 
 * @author Al Mele
 *
 */
public class LaunchPad {
	protected static Logger log = LogManager.getLogger(LaunchPad.class);
	static protected NpcGeneratorUI npcWin = null;
	static protected RollerInterface rollerWin = null;
	static protected InitiativeManager initManWin = null;
	
	static Image icon = null;
	
	public static Image getIcon() {
		return icon;
	}
	
	/**
	 * Load the log4j2 configuration file and establish the logging configuration.
	 */
	static public void configureLogging() {
		LoggerContext context = (org.apache.logging.log4j.core.LoggerContext) LogManager.getContext(false);
		URL loggingConfig = LaunchPad.class.getClassLoader().getResource(
				"com/deathfrog/utils/log4j2.xml");
		if (loggingConfig != null) {
			System.out.println("Logging with config: "
					+ loggingConfig.getFile());

			try {
				context.setConfigLocation(loggingConfig.toURI());
			} catch (URISyntaxException e) {
				System.out.println("Error while configuring logging: " + GameException.fullExceptionInfo(e));
			}
		} else {
			System.out.println("Logging configuration not found.");
		}
	}
	
	/**
	 * @param menuTool
	 */
	static protected void setupDiceRoller(Menu menuTool) {
		MenuItem miDiceRoller = new MenuItem(menuTool, SWT.NONE);
		miDiceRoller.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (rollerWin == null) {
					rollerWin = new RollerInterface();
				}
				
				if (!rollerWin.isOpen()) {
					rollerWin.open();
				} else { 
					rollerWin.getShell().forceActive();
					rollerWin.getShell().forceFocus();					
				}
			}
		});
		miDiceRoller.setText("&Dice Roller");		
	}
	
	/**
	 * @param menuTool
	 */
	static protected void setupNPCGenerator(Menu menuTool) {
		MenuItem miGenerator = new MenuItem(menuTool, SWT.NONE);
		miGenerator.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (npcWin == null) {
					npcWin = new NpcGeneratorUI();
				}
				
				if (!npcWin.isOpen()) {
					npcWin.open();
				} else {
					npcWin.getShell().forceActive();
					npcWin.getShell().forceFocus();
				}
			}
		});
		miGenerator.setText("&NPC Generator");		
	}
	
	
	/**
	 * @param menuTool
	 */
	static protected void setupInitiativeTracker(Menu menuTool) {
		MenuItem miInitiative = new MenuItem(menuTool, SWT.NONE);
		miInitiative.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (initManWin == null || initManWin.getShell().isDisposed()) {
					initManWin = new InitiativeManager();
				}
				
				if (!initManWin.isOpen()) {
					initManWin.open();
				} else { 
					initManWin.getShell().forceActive();
					initManWin.getShell().forceFocus();					
				}
			}
		});
		miInitiative.setText("&Initiative");
	}
	
	
	/**
	 * Configure and display the launch pad.
	 */
	static protected void openLaunchPad() {
		Display display = Display.getDefault();
		Shell shlMerisylDivineTools = new Shell();
		shlMerisylDivineTools.setMinimumSize(new Point(800, 600));
		
		shlMerisylDivineTools.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (npcWin != null) {
					npcWin.close();
				}
				if (rollerWin != null) {
					rollerWin.close();
				}
				if (initManWin != null) {
					initManWin.close();
				}
				
				SWTResourceManager.disposeAll();
			}
		});
		
		icon = new Image(display, LaunchPad.class.getResourceAsStream("/com/deathfrog/utils/Patrael.png"));
		
		shlMerisylDivineTools.setImage(icon);
		shlMerisylDivineTools.setSize(450, 300);
		shlMerisylDivineTools.setText("Merisyl Launch Pad");
		shlMerisylDivineTools.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		Menu menu = new Menu(shlMerisylDivineTools, SWT.BAR);
		shlMerisylDivineTools.setMenuBar(menu);
		
		MenuItem mntmtools = new MenuItem(menu, SWT.CASCADE);
		mntmtools.setText("&Tools");
		
		Menu menuTool = new Menu(mntmtools);
		mntmtools.setMenu(menuTool);
		

		setupNPCGenerator(menuTool);
		setupDiceRoller(menuTool);
		setupInitiativeTracker(menuTool);
		
		Browser browser = new Browser(shlMerisylDivineTools, SWT.NONE);
		browser.setUrl("www.merisyl.com");

		shlMerisylDivineTools.open();
		shlMerisylDivineTools.layout();
		
		while (!shlMerisylDivineTools.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}		
	}
	
	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try
		{
			configureLogging();
			openLaunchPad();
		} catch (RuntimeException re) {
			log.error(GameException.fullExceptionInfo(re));
		}

	}
}
