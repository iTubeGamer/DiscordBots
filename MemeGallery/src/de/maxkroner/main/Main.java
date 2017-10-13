package de.maxkroner.main;

import java.util.Scanner;

import de.maxkroner.implementation.Bot;
import de.maxkroner.implementation.JokeBot;
import de.maxkroner.implementation.privateBot.PrivateBot;
import de.maxkroner.ui.UserInput;

public class Main {

	private static Scanner scanner = new Scanner(System.in);
	private static UserInput userInput = new UserInput(scanner);
	private static Bot bot;

	public static void main(String[] args) {
		startBotLaunchMenue();
	}

	private static void startBotLaunchMenue() {

		int auswahl = userInput.getMultipleChoiceResult("Which bot should be started?", "JokeBot", "PrivateBot");
		switch (auswahl) {
		case 1:
			bot = new JokeBot(scanner, userInput);
			break;
		case 2:
			bot = new PrivateBot(scanner, userInput);
			break;
		}

	}

	public static void exit() {
		bot.disconnect();
		System.out.println("finished");
		System.exit(0);
	}

}
