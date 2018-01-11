package de.maxkroner.main;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Scanner;

import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.labelers.TimestampLabeler;
import org.pmw.tinylog.policies.DailyPolicy;
import org.pmw.tinylog.writers.RollingFileWriter;

import de.maxkroner.implementation.Bot;
import de.maxkroner.implementation.TempChannelBot;
import de.maxkroner.ui.UserInput;


public class Main {
	public static Bot bot;
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
			String filename = Paths.get(home, "discordBots", "tempChannels", "log", "tc.log").toString() ;
			Configurator.fromResource("tinylog.properties")
						.addWriter(new RollingFileWriter(filename, 1000, new TimestampLabeler("yyyy-MM-dd"), new DailyPolicy()))
						.activate();
		} catch (IOException e) {
			e.printStackTrace();
		}
	
		String token = args[0];			
		bot = new TempChannelBot(token, scanner, userInput);
		Runtime.getRuntime().addShutdownHook(new ShutdownHook(bot));
	}

}
