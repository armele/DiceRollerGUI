package com.deathfrog.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

public class GameException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public GameException (String message) {
		super(message);
	}
	
	public GameException (String message, Throwable source) {
		super(message, source);
	}
	
	/**
	 * @param t
	 * @return
	 */
	static public String fullExceptionInfo(Throwable t) {
		StringBuffer buf = new StringBuffer();
		
		if (t != null) {
			if (t.getLocalizedMessage() == null) {
				buf.append(t);
			} else {
				buf.append(t.getLocalizedMessage());
			}
			buf.append("\nCaused by:");
			buf.append(fullExceptionInfo(t.getCause()));
			buf.append("\nStack:");
			
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			t.printStackTrace(pw);

			buf.append(sw.toString());
		} else {
			buf.append("(null)");
		}
		
		return buf.toString();
	}
}
