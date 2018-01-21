package com.deathfrog.utils.ui;

import java.util.HashMap;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;

/**
 * This class supports a reference-count based management of SWT resources that need to be
 * explicitly disposed of.  Resources should be created using their associated "create<>Resource" 
 * methods, and released using their "release<>Resource" methods.
 * 
 * This class will track reference counting and dispose of the resources when the count reaches zero.
 * 
 * @author Al Mele
 *
 */
public class SWTResourceManager {
	protected static HashMap<Object, Integer> referenceCounts = new HashMap<Object, Integer>();
	protected static HashMap<FontData, Font> fontResources = new HashMap<FontData, Font>();
	protected static HashMap<RGB, Color> colorResources = new HashMap<RGB, Color>();
	
	
	/**
	 * @param fd
	 * @return
	 */
	static public Font createFontResource(Device device, FontData fd) {
		Font savedFont = fontResources.get(fd);
		
		if (savedFont == null) {
			savedFont = new Font(device, fd);
			Integer refCount = new Integer(1);
			referenceCounts.put(savedFont, refCount);
			fontResources.put(fd, savedFont);
		} else {
			Integer refCount = referenceCounts.get(savedFont);
			refCount++;
			referenceCounts.put(savedFont, refCount);
		}
		
		return savedFont;
	}
	
	
	/**
	 * @param font
	 */
	static public int releaseFontResource(Font font) {
		Integer refCount = referenceCounts.get(font);
		
		if (refCount != null) {
			refCount--;
			referenceCounts.put(font, refCount);
			
			if (refCount == 0) {
				font.dispose();
				referenceCounts.remove(font);
				
				FontData removeKey = null;
				for (FontData fd : fontResources.keySet()) {
					Font entry = fontResources.get(fd);
					if (entry.equals(font)) {
						removeKey = fd;
						break;
					}
				}
				
				if (removeKey != null) {
					fontResources.remove(removeKey);
				}
			}
			
		} else {
			refCount = 0;
		}
		
		return refCount;
	}
	
	/**
	 * @param obj
	 * @return
	 */
	static public int checkReferenceCount(Object obj) {
		int refCount = 0;
		
		if (obj instanceof FontData) {
			refCount = referenceCounts.get(fontResources.get(obj));
		} else if (obj instanceof Font) {
			Integer referenceCount = referenceCounts.get(obj);
			if (referenceCount != null) {
				refCount = referenceCount;
			}
		}
		
		return refCount;
	}
	
	
	/**
	 * @param fd
	 * @return
	 */
	static public Color createColorResource(Device device, RGB colorspec) {
		Color savedColor = colorResources.get(colorspec);
		
		if (savedColor == null) {
			savedColor = new Color(device, colorspec);
			Integer refCount = new Integer(1);
			referenceCounts.put(savedColor, refCount);
			colorResources.put(colorspec, savedColor);
		} else {
			Integer refCount = referenceCounts.get(savedColor);
			refCount++;
			referenceCounts.put(savedColor, refCount);
		}
		
		return savedColor;
	}
	
	
	/**
	 * @param font
	 */
	static public int releaseColorResource(Color color) {
		Integer refCount = referenceCounts.get(color);
		
		if (refCount != null) {
			refCount--;
			referenceCounts.put(color, refCount);
			
			if (refCount == 0) {
				color.dispose();
				referenceCounts.remove(color);
				
				RGB removeKey = null;
				for (RGB colorspec : colorResources.keySet()) {
					Color entry = colorResources.get(colorspec);
					if (entry.equals(color)) {
						removeKey = colorspec;
						break;
					}
				}
				
				if (removeKey != null) {
					colorResources.remove(removeKey);
				}
			}
			
		} else {
			refCount = 0;
		}
		
		return refCount;
	}
	
}
