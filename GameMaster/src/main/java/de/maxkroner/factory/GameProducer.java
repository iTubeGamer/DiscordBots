package de.maxkroner.factory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import de.maxkroner.model.GameService;
import de.maxkroner.model.IGame;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

public class GameProducer {
	private Map<String, IGameFactory> gameFactories = new HashMap<>();
	
	private Optional<IGameFactory> getOptionalGameFactory(String name){
		return Optional.ofNullable(gameFactories.get(name));
	}
	
	public void addGameFactory(IGameFactory gameFactory){
		gameFactories.put(gameFactory.getGameCommand(), gameFactory);
	}

	public Optional<IGame> createGame(GameService gameService, String name, MessageReceivedEvent event){

		return getOptionalGameFactory(name).map(T -> Optional.of(T.createGame(gameService, event))).orElse(Optional.empty());
	}
}
