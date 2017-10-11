package de.maxkroner.ui;

import java.util.Scanner;

import de.maxkroner.implementation.Bot;

public class PrivateBotMenue extends BotMenue
{

	public PrivateBotMenue(Scanner scanner, UserInput userInput) {
		super(scanner, userInput);
		
	}
	
	public void startMenue(Bot bot){
		super.startMenue(bot);
		customizeBot();
	}

}
