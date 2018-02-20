package com.deathfrog.utils;

import java.io.IOException;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * @author Al Mele
 *
 */
public class JsonUtils {
	protected static Logger log = LogManager.getLogger(JsonUtils.class);
	
	/**
	 * Null safe method of getting a Json attribute from a parent element as a string.
	 * 
	 * @param je
	 * @param attributeName
	 * @return
	 */
	public static String getJsonString(JsonElement je,  String attributeName) {
		String result = null;
		JsonObject jobj = je.getAsJsonObject();
		
		if (jobj != null) {
			JsonElement attrElem = jobj.get(attributeName);
			
			if (attrElem != null) {
				result = attrElem.getAsString();
			}
		}
		
		return result;
	}
	
	/**
	 * @param in
	 * @return
	 * @throws IOException
	 */
	public static JsonArray readJsonStream(Scanner in, String arrayName) throws IOException {
		StringBuilder sb = new StringBuilder();
		while (in.hasNext()) {
			String next = in.nextLine();
			sb.append(next);
			log.debug(next);
		}
		in.close();

		JsonParser parser = new JsonParser();
		JsonElement element = parser.parse(sb.toString());
		JsonObject jobject = element.getAsJsonObject();
		JsonArray jarray = jobject.getAsJsonArray(arrayName);

		return jarray;
	}
}
