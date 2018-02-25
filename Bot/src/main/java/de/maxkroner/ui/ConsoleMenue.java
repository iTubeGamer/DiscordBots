package de.maxkroner.ui;

import de.maxkroner.implementation.Bot;

public class ConsoleMenue implements IConsoleMenue {
	protected Bot bot;
	
	public ConsoleMenue(Bot bot){
		this.bot = bot;
	}

	public ConsoleMenue() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void startMenue(Bot bot) {
		customizeBot();
	}
	
	protected void customizeBot(){
		int auswahl = 0;
		
		while (!(auswahl==4)){
			auswahl = UserInput.getMultipleChoiceResult("What do you want to do?", "change bot name", "change bot playing text", "change bot avatar", "exit menue");
			switch(auswahl){
			case 1: 
				bot.changeName(UserInput.getStringAnswer("enter new bot name:")); break;
			case 2:
				bot.changePlayingText(UserInput.getStringAnswer("enter new playing text:")); break;
			case 3:
				bot.changeAvatar(UserInput.getStringAnswer("enter url to image:"), UserInput.getStringAnswer("enter image type (jpeg, png, etc.):")); break;
			}
		}
		
	}
		
}
