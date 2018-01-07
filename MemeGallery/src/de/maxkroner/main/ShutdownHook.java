package de.maxkroner.main;

import org.pmw.tinylog.Logger;

public class ShutdownHook extends Thread{
	
	@Override
	public void run() {
		Logger.info("Shutdown Hook started");
		Main.bot.disconnect();
	}
}
