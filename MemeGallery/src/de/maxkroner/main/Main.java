package de.maxkroner.main;

import java.util.Scanner;

import de.maxkroner.implementation.JokeBot;
import de.maxkroner.model.IBot;

public class Main {
	
	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		
		if (args.length < 1){ // Needs a bot token provided
			scanner.close();
			throw new IllegalArgumentException("Please provide the Bot-Token as argument!");
		}
		
		displayStartMenue();
		int input = scanner.nextInt();
		createBot(input, args[0]);
		scanner.close();
	}
	
	private static void displayStartMenue(){
		System.out.println("Which Bot should be started?");
		System.out.println("(1) Joke Bot");
	}
	
	private static IBot createBot(int input, String token){
		IBot bot = null;
		
		switch  (input) {
		case 1:  bot = new JokeBot(token); break;
		}
		
		return bot;
	}
	
	 

}
