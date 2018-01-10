package de.maxkroner.ui;

import java.util.Scanner;

import de.maxkroner.implementation.Bot;

public class BotMenue implements IBotMenue {
	protected Bot bot;
	protected Scanner scanner;
	protected UserInput userInput;
	
	public BotMenue(Scanner scanner, UserInput userInput){
		this.scanner = scanner;
		this.userInput = userInput;
	}

	@Override
	public void startMenue(Bot bot) {
		this.bot = bot;
	}
	
	protected void customizeBot(){
		int auswahl = 0;
		
		while (!(auswahl==4)){
			auswahl = userInput.getMultipleChoiceResult("What do you want to do?", "change bot name", "change bot playing text", "change bot avatar", "exit menue");
			switch(auswahl){
			case 1: 
				bot.changeName(userInput.getStringAnswer("enter new bot name:")); break;
			case 2:
				bot.changePlayingText(userInput.getStringAnswer("enter new playing text:")); break;
			case 3:
				bot.changeAvatar(userInput.getStringAnswer("enter url to image:"), userInput.getStringAnswer("enter image type (jpeg, png, etc.):")); break;
			}
		}
		
	}
		
}
