package de.maxkroner.main;

import de.maxkroner.implementation.Bot;

public class ShutdownHook extends Thread{
	private Bot bot;
	
	public ShutdownHook(Bot bot) {
		super();
		this.bot = bot;
	}

	@Override
	public void run() {
		bot.disconnect();
	}
}
