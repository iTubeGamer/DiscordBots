package de.maxkroner.main;

import java.util.Scanner;

import de.maxkroner.implementation.Bot;
import de.maxkroner.implementation.JokeBot;
import de.maxkroner.ui.UserInput;


public class Main{
	public static Bot bot;
	private static Scanner scanner = new Scanner(System.in);
	private static UserInput userInput = new UserInput(scanner);

	public static void main(String[] args) {
		if (args.length < 1){
			System.out.println("Please provide a token as argument.");
			System.exit(1);
		}
		
		String token = args[0];			
		bot = new JokeBot(token, scanner, userInput);
		Runtime.getRuntime().addShutdownHook(new ShutdownHook(bot));
	}

}
