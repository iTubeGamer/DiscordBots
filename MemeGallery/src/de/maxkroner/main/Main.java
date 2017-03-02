package de.maxkroner.main;

import java.util.Scanner;

import de.maxkroner.implementation.JokeBot;
import de.maxkroner.ui.ConsoleMenue;
import de.maxkroner.ui.UserInput;
import die.maxkroner.database.JokeDatabase;

public class Main {

	private static JokeBot bot;
	private static Scanner scanner = new Scanner(System.in);
	private static JokeDatabase jokeDatabase = new JokeDatabase();
	private static UserInput userInput = new UserInput(scanner);
	private static ConsoleMenue consoleMenue = new ConsoleMenue(userInput, scanner, jokeDatabase);


	public static void main(String[] args) {
		startBot(args);
	}

	private static void startBot(String[] args) {
		if (args.length < 1) { // Needs a bot token provided
			throw new IllegalArgumentException("Please provide the Bot-Token as argument!");
		}
		bot = new JokeBot(args[0], consoleMenue, jokeDatabase);
	}

	public static void exit() {
		jokeDatabase.close();
		bot.disconnect();
		System.out.println("finished");
		System.exit(0);
	}

}
