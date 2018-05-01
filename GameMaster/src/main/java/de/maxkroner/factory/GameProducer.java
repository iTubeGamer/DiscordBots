package de.maxkroner.factory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import de.maxkroner.model.IGameService;
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

	public Optional<IGame> createGame(IGameService gameService, List<String> args, MessageReceivedEvent event){
		String name = args.get(0);
		args.remove(name);

		return getOptionalGameFactory(name).map(T -> Optional.of(T.createGame(event, args))).orElse(Optional.empty());
	}
	
	public void initializeGameModes(){
		for (IGameFactory factory : gameFactories.values()){
			factory.initializeGameMode();
		}
	}
}
