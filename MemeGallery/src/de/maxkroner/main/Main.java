package de.maxkroner.main;

import java.util.Scanner;

import de.maxkroner.implementation.JokeBot;
import de.maxkroner.ui.ConsoleMenue;
import de.maxkroner.ui.UserInput;
import die.maxkroner.database.JokeDatabase;

public class Main {

	private static JokeBot bot;
	private static Scanner scanner = new Scanner(System.in);
	private static UserInput userInput = new UserInput(scanner);
	private static ConsoleMenue consoleMenue = new ConsoleMenue(userInput, scanner);


	public static void main(String[] args) {
		startBot(args);
		JokeDatabase.connect();
	}

	private static void startBot(String[] args) {
		if (args.length < 1) { // Needs a bot token provided
			throw new IllegalArgumentException("Please provide the Bot-Token as argument!");
		}
		bot = new JokeBot(args[0], consoleMenue);
	}

	public static void exit() {
		JokeDatabase.disconnect();
		bot.disconnect();
		System.out.println("finished");
		System.exit(0);
	}

}
