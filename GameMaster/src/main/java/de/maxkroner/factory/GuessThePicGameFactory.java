package de.maxkroner.factory;

import de.maxkroner.model.GuessThePicGame;
import de.maxkroner.model.IGame;
import de.maxkroner.values.Strings;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

public class GuessThePicGameFactory implements IGameFactory{

	@Override
	public String getGameName() {
		return Strings.GUESS_THE_PIC_NAME;
	}
	
	@Override
	public String getGameCommand() {
		return Strings.GUESS_THE_PIC_COMMAND;
	}

	@Override
	public IGame createGame(MessageReceivedEvent event) {
		return new GuessThePicGame(event.getChannel());
	}

}
