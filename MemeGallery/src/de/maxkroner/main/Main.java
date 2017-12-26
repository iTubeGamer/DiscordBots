package de.maxkroner.main;

import java.util.Scanner;

import de.maxkroner.implementation.Bot;
import de.maxkroner.implementation.JokeBot;
import de.maxkroner.implementation.ScamBot;
import de.maxkroner.implementation.privateBot.PrivateBot;
import de.maxkroner.ui.UserInput;

public class Main {

	private static Scanner scanner = new Scanner(System.in);
	private static UserInput userInput = new UserInput(scanner);
	private static Bot bot;

	public static void main(String[] args) {
		switch (args.length) {
		case 0:
			System.out.println("Please provide a token as argument.");
			System.exit(1);
			break;
		case 1:
			startBotLaunchMenue(args[0]);
		case 2:
			startBot(args[0], args[1]);
		}

	}

	private static void startBot(String botName, String token) {
		switch (botName) {
		case "JokeBot":
			new JokeBot(token, scanner, userInput);
			break;
		case "PrivateBot":
			new PrivateBot(token, scanner, userInput);
			break;
		case "ScamBot":
			new ScamBot(token, scanner, userInput);
			break;
		}

	}

	private static void startBotLaunchMenue(String token) {

		int auswahl = userInput.getMultipleChoiceResult("Which bot should be started?", "JokeBot",
				"PrivateBot", "ScamBot");
		switch (auswahl) {
		case 1:
			new JokeBot(token, scanner, userInput);
			break;
		case 2:
			new PrivateBot(token, scanner, userInput);
			break;
		case 3:
			new ScamBot(token, scanner, userInput);
			break;
		default:
			System.out.println(
					"The argument containted no valid bot name. Please provide either one argument with the token or a bot name in the first argument and the token in the second argument.");
			System.exit(1);
		}

		
	}

	public static void exit() {
		bot.disconnect();
		System.out.println("finished");
		System.exit(0);
	}

}
