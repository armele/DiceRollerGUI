package org.eclipse.wb.swt;

import static org.junit.Assert.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.junit.Test;

import com.deathfrog.utils.ui.SWTResourceManager;

public class TestSWTResourceManager {
	@Test
	public void testFontReferenceCounts() {
		Device device = Display.getCurrent ();
		Shell imShell = new Shell();
		Group testGroup = new Group(imShell, SWT.NONE);
		Group testGroup2 = new Group(imShell, SWT.NONE);
		
		// Should have no impact - font has no reference count.
		assertEquals(0, SWTResourceManager.releaseFontResource(testGroup.getFont()));
		
		FontData[] fD = testGroup.getFont().getFontData();
		fD[0].setHeight(fD[0].getHeight() * 2);
		
		// Create a new font and set it.
		Font fontUnderTest = SWTResourceManager.createFontResource(device, fD[0]);
		testGroup.setFont(fontUnderTest);
		assertEquals(1, SWTResourceManager.checkReferenceCount(testGroup.getFont()));
		
		// Release the previous font for the second SWT widget and then replace it.
		assertEquals(0, SWTResourceManager.releaseFontResource(testGroup2.getFont()));
		testGroup2.setFont(SWTResourceManager.createFontResource(device, fD[0]));
		assertEquals(2, SWTResourceManager.checkReferenceCount(testGroup2.getFont()));
		
		// Verify that the same object was used for both groups (fetched from cache);
		assertEquals(fontUnderTest, testGroup2.getFont());
		
		assertEquals(1, SWTResourceManager.releaseFontResource(fontUnderTest));
		assertEquals(0, SWTResourceManager.releaseFontResource(fontUnderTest));
		
		assertTrue(testGroup.getFont().isDisposed());
		assertTrue(testGroup2.getFont().isDisposed());
	}
}
