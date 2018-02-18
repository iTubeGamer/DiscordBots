package de.maxkroner.main;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Scanner;

import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.labelers.TimestampLabeler;
import org.pmw.tinylog.policies.DailyPolicy;
import org.pmw.tinylog.writers.RollingFileWriter;

import de.maxkroner.factory.GameProducer;
import de.maxkroner.gtp.implementation.GuessThePicGameFactory;
import de.maxkroner.implementation.GameMasterBot;
import de.maxkroner.ui.UserInput;


public class Main {

	private static Scanner scanner = new Scanner(System.in);
	private static UserInput userInput = new UserInput(scanner);

	public static void main(String[] args) {
		if (args.length < 1){
			System.out.println("Please provide a token as argument.");
			System.exit(1);
		}
		
		//configure logging folder		
		try {
			String home = System.getProperty("user.home");;
			String filename = Paths.get(home, "discordBots", "GameMaster", "log", "gm.log").toString() ;
			Configurator.fromResource("tinylog.properties")
						.addWriter(new RollingFileWriter(filename, 1000, new TimestampLabeler("yyyy-MM-dd"), new DailyPolicy()))
						.activate();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//add gameFactories 
		GameProducer gameProducer = new GameProducer();
		gameProducer.addGameFactory(new GuessThePicGameFactory());
	
		String token = args[0];			
		GameMasterBot bot = new GameMasterBot(token, scanner, userInput);
		bot.setGameProducer(gameProducer);
		Runtime.getRuntime().addShutdownHook(new ShutdownHook(bot));
	}

}
