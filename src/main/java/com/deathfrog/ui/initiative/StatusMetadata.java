package com.deathfrog.ui.initiative;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Control;

import com.deathfrog.utils.ui.LaunchPad;
import com.deathfrog.utils.ui.SWTResourceManager;

/**
 * @author Al Mele
 *
 */
public class StatusMetadata {
	protected static int LINE_WRAP_WIDTH = 50;
	
	protected String name;
	protected String color;
	protected String iconName;
	protected String description;
	protected String wrappedDescription = null;
	protected Color  swtColor = null;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	public String getColor() {
		return color;
	}
	public void setColor(String color) {
		this.color = color;
	}
	public String getIconName() {
		return iconName;
	}
	public void setIconName(String icon) {
		this.iconName = icon;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * Get the Image associated with the metadata icon name
	 * 
	 * @return
	 */
	public Image getIcon(Control c) {
		return SWTResourceManager.createImageResource(c, getIconName());
	}
	

	/**
	 * Return the color object defined by the associated RGB metadata string.
	 * @return
	 */
	public Color getSWTColor(Control c) {
		if  ((swtColor == null || swtColor.isDisposed()) && c != null) {
			String[] rgb = getColor().split(",");
			RGB colorspec = new RGB(new Integer(rgb[0]), new Integer(rgb[1]), new Integer(rgb[2]));
			swtColor = SWTResourceManager.createColorResource(c, colorspec);
		}
		
		return swtColor;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(name).append(System.getProperty("line.separator"));
		
		if (wrappedDescription == null) {
			StringBuffer descWrap = new StringBuffer();
			int count = 0;
			for (int i = 0; i < description.length(); i++) {
				if (count < LINE_WRAP_WIDTH || description.charAt(i) != ' ') {
					descWrap.append(description.charAt(i));
				} else {
					count = 0;
					descWrap.append(System.getProperty("line.separator"));
				}
				count++;
			}
			wrappedDescription = descWrap.toString();
		}
		
		sb.append(wrappedDescription);
		
		return sb.toString();
	}
	
}
