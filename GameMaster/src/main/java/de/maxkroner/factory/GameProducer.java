package de.maxkroner.factory;

import java.util.Map;
import java.util.Optional;

import de.maxkroner.model.IGame;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;

public class GameProducer {
	private Map<String, IGameFactory> gameFactories;
	
	private Optional<IGameFactory> getOptionalGameFactory(String name){
		return Optional.ofNullable(gameFactories.get(name));
	}
	
	public void addGameFactory(IGameFactory gameFactory){
		gameFactories.put(gameFactory.getGameCommand(), gameFactory);
	}

	public Optional<IGame> createGame(String name, MessageReceivedEvent event){
		
		getOptionalGameFactory(name).map(T -> Optional.of(T.createGame(event))).orElse(Optional.empty());
		
		return null;
	}
}
