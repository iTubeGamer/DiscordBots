package de.maxkroner.factory;

import de.maxkroner.model.GameService;
import de.maxkroner.model.GuessThePicGame;
import de.maxkroner.model.IGame;
import de.maxkroner.values.Values;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

public class GuessThePicGameFactory implements IGameFactory{

	@Override
	public String getGameName() {
		return Values.GAME_GUESS_THE_PIC_NAME;
	}
	
	@Override
	public String getGameCommand() {
		return Values.GAME_GUESS_THE_PIC_COMMAND;
	}

	@Override
	public IGame createGame(GameService gameService, MessageReceivedEvent event) {
		GuessThePicGame game = new GuessThePicGame(gameService, event.getChannel(), event.getAuthor());
		game.addPlayer(event.getAuthor());
		return game;	
	}

}
