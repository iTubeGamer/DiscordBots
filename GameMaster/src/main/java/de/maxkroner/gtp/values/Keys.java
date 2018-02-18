package de.maxkroner.gtp.values;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.pmw.tinylog.Logger;

public class Keys {

	public static final String google_search_id = "014357723537713710519:earmz8ohk6k"; //custom google search id
	
	private static ConcurrentLinkedQueue<String> keys = new ConcurrentLinkedQueue<>();
	
	public static void readKeys(){
		String line = "";
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		InputStream is = classloader.getResourceAsStream("keys.txt");

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			if (is != null) {
				while ((line = reader.readLine()) != null) {
					if(checkKey(line)){
						keys.add(line);
					}
				}
			}
		} catch (IOException e) {
			Logger.error(e);
		}
	}
	
	private static boolean checkKey(String line) {
		// TODO
		return true;
	}

	public static String getKey(){
		return keys.peek();
	}
	
	public static void removeKey(String key){
		if(keys.peek().equals(key)){
			keys.remove();
		}
	}
	
	public static boolean hasNext(){
		return !keys.isEmpty();
	}
	
	
}
