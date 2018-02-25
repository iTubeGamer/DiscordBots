package de.maxkroner.main;

import de.maxkroner.factory.GameProducer;
import de.maxkroner.gtp.implementation.GuessThePicGameFactory;
import de.maxkroner.implementation.GameMasterBot;


public class Main {

	public static void main(String[] args) {
		if (args.length < 1){
			System.out.println("Please provide a token as argument.");
			System.exit(1);
		}
		
		//add gameFactories 
		GameProducer gameProducer = new GameProducer();
		gameProducer.addGameFactory(new GuessThePicGameFactory());
	
		String token = args[0];			
		GameMasterBot bot = new GameMasterBot();
		bot.setGameProducer(gameProducer);
		bot.addLogging("gm");
		bot.addDatabase("GameMaster");
		bot.run(token);
		Runtime.getRuntime().addShutdownHook(new ShutdownHook(bot));
	}

}
