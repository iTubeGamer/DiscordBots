package de.maxkroner.main;

import de.maxkroner.implementation.Bot;
import de.maxkroner.implementation.JokeBot;


public class Main{
	public static Bot bot;

	public static void main(String[] args) {
		if (args.length < 1){
			System.out.println("Please provide a token as argument.");
			System.exit(1);
		}
		
		String token = args[0];			
		bot = new JokeBot(token);
		Runtime.getRuntime().addShutdownHook(new ShutdownHook(bot));
	}

}
