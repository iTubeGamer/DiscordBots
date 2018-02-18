package de.maxkroner.gtp.implementation;

import java.sql.SQLException;
import java.util.List;

import org.pmw.tinylog.Logger;

import de.maxkroner.factory.IGameFactory;
import de.maxkroner.model.GameService;
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
	public IGame createGame(GameService gameService, MessageReceivedEvent event, List<String> args) {
		GuessThePicGame game = null;
		if(args.size() == 1){
			try{
				Long list_id = GuessThePicGame.getDatabase().getListIdForList(event.getGuild().getLongID(), args.get(0));
				game = new GuessThePicGame(gameService, event.getChannel(), event.getAuthor(), list_id);
			} catch (SQLException e){
				Logger.error("Could not create GTP game.");
				event.getChannel().sendMessage(Values.getMessageString(Values.MESSAGE_GTP_INVALID_LIST, args.get(0)));
				return null;
			}
		} else if (args.isEmpty()){
			game = new GuessThePicGame(gameService, event.getChannel(), event.getAuthor());
		} else {
			return null;
		}
		
		game.addPlayer(event.getAuthor());
		return game;	
	}

}
