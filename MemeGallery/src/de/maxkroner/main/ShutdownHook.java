package de.maxkroner.main;

import org.pmw.tinylog.Logger;

public class ShutdownHook extends Thread{
	
	@Override
	public void run() {
		Main.bot.disconnect();
		Logger.info("|||---SHUTTING DOWN---|||");
	}
}
