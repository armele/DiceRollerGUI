package com.deathfrog.utils.ui;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Resource;
import org.eclipse.swt.widgets.Control;

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
	protected static Logger log = LogManager.getLogger(SWTResourceManager.class);
	
	protected static HashMap<Object, Integer> referenceCounts = new HashMap<Object, Integer>();
	protected static HashMap<FontData, Font> fontResources = new HashMap<FontData, Font>();
	protected static HashMap<RGB, Color> colorResources = new HashMap<RGB, Color>();
	protected static HashMap<String, Image> imageResources = new HashMap<String, Image>();
	
	/**
	 * @param fd
	 * @return
	 */
	static public Font createFontResource(Control c, FontData fd) {
		Font savedFont = fontResources.get(fd);
		
		if (savedFont == null) {
			savedFont = new Font(c.getDisplay(), fd);
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
	 * Release the reference count on the specified font resource.
	 * 
	 * @param font
	 * @return
	 */
	static public int releaseFontResource(Font font) {
		return releaseResource(font, fontResources);
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
	static public Color createColorResource(Control c, RGB colorspec) {
		Color savedColor = colorResources.get(colorspec);
		
		if (savedColor == null) {
			savedColor = new Color(c.getDisplay(), colorspec);
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
	 * Releases the reference count on the specified color resource.
	 * 
	 * @param color
	 * @return
	 */
	static public int releaseColorResource(Color color) {
		return releaseResource(color, colorResources);
	}
	
	/**
	 * Disposes of all managed resources remaining.
	 */
	public static void disposeAll() {
		for (Font f : fontResources.values()) {
			f.dispose();
		}
		fontResources.clear();
		
		for (Color c : colorResources.values()) {
			c.dispose();
		}
		colorResources.clear();
		
		for (Image i : imageResources.values()) {
			i.dispose();
		}
		imageResources.clear();
	}
	
	
	/**
	 * @param fd
	 * @return
	 */
	static public Image createImageResource(Control c, String imageName) {
		Image image = imageResources.get(imageName);
		
		if (image == null) {
			InputStream imageStream = LaunchPad.class.getResourceAsStream("/com/deathfrog/utils/" + imageName);
			if (imageStream != null) {
				image = new Image(c.getDisplay(), imageStream);
			}
			if (image == null) {
				log.error("No image file found: " + imageName);
			} else {
				Integer refCount = new Integer(1);
				referenceCounts.put(image, refCount);
				imageResources.put(imageName, image);
			}
		} else {
			Integer refCount = referenceCounts.get(image);
			refCount++;
			referenceCounts.put(image, refCount);
		}
		
		return image;
	}
	
	/**
	 * Release the reference count on the given image resource.
	 * 
	 * @param image
	 * @return
	 */
	static public int releaseImageResource(Image image) {
		return releaseResource(image, imageResources);
	}

	
	/**
	 * Given an SWT disposable resource, reduce the reference count or dispose of the resource, as appropriate.
	 * 
	 * @param resource
	 * @param resourceCache
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	static protected int releaseResource(Resource resource, Map resourceCache) {
		Integer refCount = referenceCounts.get(resource);
		
		if (refCount != null) {
			refCount--;
			referenceCounts.put(resource, refCount);
			
			if (refCount == 0) {
				resource.dispose();
				referenceCounts.remove(resource);
				
				Object cacheKey = null;
				for (Object key : resourceCache.keySet()) {
					Object entry = resourceCache.get(key);
					if (entry.equals(resource)) {
						cacheKey = key;
						break;
					}
				}
				
				if (cacheKey != null) {
					resourceCache.remove(cacheKey);
				}
			}
			
		} else {
			refCount = 0;
		}
		
		return refCount;
	}
}
