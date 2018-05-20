package de.maxkroner.ui;

import de.maxkroner.implementation.Bot;

public class ConsoleMenue implements IConsoleMenue {
	protected Bot bot;
	
	public ConsoleMenue(Bot bot){
		this.bot = bot;
	}

	@Override
	public void startMenue(Bot bot) {
		customizeBot();
	}
	
	protected void customizeBot(){
		int auswahl = 0;
		
		while (!(auswahl==4)){
			auswahl = ConsoleUserInput.getMultipleChoiceResult("What do you want to do?", "change bot name", "change bot playing text", "change bot avatar", "exit menue");
			switch(auswahl){
			case 1: 
				bot.changeName(ConsoleUserInput.getStringAnswer("enter new bot name:")); break;
			case 2:
				bot.changePlayingText(ConsoleUserInput.getStringAnswer("enter new playing text:")); break;
			case 3:
				bot.changeAvatar(ConsoleUserInput.getStringAnswer("enter url to image:"), ConsoleUserInput.getStringAnswer("enter image type (jpeg, png, etc.):")); break;
			}
		}
		
	}
		
}
