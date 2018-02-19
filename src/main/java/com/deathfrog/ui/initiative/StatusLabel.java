package com.deathfrog.ui.initiative;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import com.google.gson.annotations.Expose;

public class StatusLabel {
	protected static int STATUS_LABEL_CORNERSIZE = 4;
	protected static int STATUS_LABEL_SIZE = 14;
	
	@Expose(serialize = true, deserialize = true)
	protected String label = null;
	
	protected Color color = null;
	protected Rectangle statusDisplayArea = null;
	protected InitiativeDisplayGroup parent = null;
	
	/**
	 * @param idgParent
	 */
	public StatusLabel(InitiativeDisplayGroup idgParent) {
		parent = idgParent;
	}
	
	/**
	 * @return
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @param label
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * @return
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * @param color
	 */
	public void setColor(Color color) {
		this.color = color;
	}

	public Rectangle getStatusDisplayArea() {
		return statusDisplayArea;
	}

	/**
	 * Draw the status label onto the initiative display group it is associated with.
	 * 
	 * @param e 
	 * @param scale the zoom scale to use when drawing
	 * @param startLoc a point where the upper right edge of the label to be drawn should be placed
	 */
	public Point paint(PaintEvent e, double scale, Point startLoc) {
		// Save the current color settings of the context (to allow us to restore them at the end).
		Color prevForeground = e.gc.getForeground();
		Color prevBackground = e.gc.getBackground();
		int prevLineSize = e.gc.getLineWidth();
		
		// Set the foreground color
		if (color != null) {
			e.gc.setBackground(color);
		} else {
			e.gc.setBackground(e.gc.getDevice().getSystemColor(SWT.COLOR_GREEN));
		}
		
		statusDisplayArea = new Rectangle((int) (startLoc.x - (STATUS_LABEL_SIZE * scale)), startLoc.y, (int)(STATUS_LABEL_SIZE * scale), (int)(STATUS_LABEL_SIZE * scale));
		
		// Draw the filled label area
		e.gc.fillRoundRectangle(statusDisplayArea.x, statusDisplayArea.y, 							// Upper left Corner
				statusDisplayArea.width, statusDisplayArea.height, 		   							// width and height
				(int)(STATUS_LABEL_CORNERSIZE * scale), (int)(STATUS_LABEL_CORNERSIZE * scale));  	// size of corner "rounding"
		
		
		// Draw the black outline
		e.gc.setLineWidth(2);
		e.gc.setForeground(e.gc.getDevice().getSystemColor(SWT.COLOR_BLACK));
		e.gc.drawRoundRectangle(statusDisplayArea.x, statusDisplayArea.y, 							// Upper left Corner
				statusDisplayArea.width, statusDisplayArea.height, 		   							// width and height
				(int)(STATUS_LABEL_CORNERSIZE * scale), (int)(STATUS_LABEL_CORNERSIZE * scale));  	// size of corner "rounding"
		
		// Restore the prior color settings in the graphics context
		e.gc.setForeground(prevForeground);
		e.gc.setBackground(prevBackground);
		e.gc.setLineWidth(prevLineSize);
		
		Point endLoc = new Point(statusDisplayArea.x, statusDisplayArea.y);
		
		return endLoc;
	}
}
