package de.maxkroner.implementation;

import java.util.Scanner;

import de.maxkroner.ui.BotMenue;
import de.maxkroner.ui.UserInput;

public class GameMasterBot extends Bot {

	public GameMasterBot(String token, Scanner scanner, UserInput userInput) {
		super(token, new BotMenue(scanner, userInput));
	}

	@Override
	public void disconnect() {
		
	}

	
}
