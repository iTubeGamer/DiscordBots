package de.maxkroner.main;

import de.maxkroner.implementation.Bot;
import de.maxkroner.implementation.TempChannelBot;


public class Main {
	public static Bot bot;

	public static void main(String[] args) {
		if (args.length < 1){
			System.out.println("Please provide a token as argument.");
			System.exit(1);
		}
	
		String token = args[0];			
		bot = new TempChannelBot(token);
		bot.addLogging("tc");
		bot.addDatabase("TempChannels");
		bot.run(token);
		Runtime.getRuntime().addShutdownHook(new ShutdownHook(bot));
	}

}
