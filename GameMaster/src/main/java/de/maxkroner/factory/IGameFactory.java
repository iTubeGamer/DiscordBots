package de.maxkroner.factory;

import de.maxkroner.model.IGame;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

public interface IGameFactory {
	public String getGameName();
	
	public String getGameCommand();
	
	public IGame createGame(MessageReceivedEvent event);
}
