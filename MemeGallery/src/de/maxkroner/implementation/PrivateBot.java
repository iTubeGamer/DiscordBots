package de.maxkroner.implementation;

import java.util.Scanner;

import de.maxkroner.ui.PrivateBotMenue;
import de.maxkroner.ui.UserInput;

public class PrivateBot extends Bot {
	private static final String token = "";

	public PrivateBot(Scanner scanner, UserInput userInput) {
		super(token, new PrivateBotMenue(scanner, userInput));
		
	}

}
