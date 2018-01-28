package de.maxkroner.implementation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import de.maxkroner.factory.GameProducer;
import de.maxkroner.model.IGame;
import de.maxkroner.parsing.Command;
import de.maxkroner.parsing.CommandHandler;
import de.maxkroner.ui.BotMenue;
import de.maxkroner.ui.UserInput;
import de.maxkroner.values.Strings;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;

public class GameMasterBot extends Bot {
	private GameProducer producer;
	private Map<IChannel, IGame> gameList = new HashMap<>();

	public GameMasterBot(String token, Scanner scanner, UserInput userInput) {
		super(token, new BotMenue(scanner, userInput));
		addCommandParsing(this.getClass(), Strings.PREFIX, '-');
	}
	
	public void setGameProducer(GameProducer producer){
		this.producer = producer;
	}

	@Override
	public void disconnect() {
		
	}
	
	@CommandHandler(Strings.COMMAND_CREATE)
	protected void createGame(MessageReceivedEvent event, Command command){
		
		if(command.getArguments().orElse(Collections.emptyList()).size() == 1){
			if(gameList.containsKey(event.getChannel())){
				event.getAuthor().getOrCreatePMChannel().sendMessage(Strings.getMessageString(Strings.CREATE_GAME_ALREADY_EXISTS, event.getChannel().getName()));
				return;
			}
			
			String name = command.getArguments().get().get(0);
			
			producer.createGame(name, event).ifPresent(T -> gameList.put(event.getChannel(), T));
			
		}
	}

	
}
