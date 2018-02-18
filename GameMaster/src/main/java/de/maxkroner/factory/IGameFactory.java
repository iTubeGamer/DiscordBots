package de.maxkroner.factory;

import java.util.List;

import de.maxkroner.model.GameService;
import de.maxkroner.model.IGame;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

public interface IGameFactory {
	public String getGameName();
	
	/**String by which the game is created
	 * 
	 * @return commandString
	 */
	public String getGameCommand();
	
	public IGame createGame(GameService gameService, MessageReceivedEvent event, List<String> args);
}
