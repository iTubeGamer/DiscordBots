package de.maxkroner.main;

import org.pmw.tinylog.Logger;

import de.maxkroner.implementation.Bot;

public class ShutdownHook extends Thread{
	private Bot bot;
	
	public ShutdownHook(Bot bot) {
		super();
		this.bot = bot;
	}

	@Override
	public void run() {
		Logger.info("Shutdown Hook started");
		bot.disconnect();
	}
}
