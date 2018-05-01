package de.maxkroner.gtp.implementation;

import java.sql.SQLException;
import java.util.List;

import org.pmw.tinylog.Logger;

import de.maxkroner.factory.IGameFactory;
import de.maxkroner.gtp.database.GTPDatabase;
import de.maxkroner.gtp.values.Keys;
import de.maxkroner.model.IGameService;
import de.maxkroner.model.IGame;
import de.maxkroner.values.Values;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

public class GuessThePicGameFactory implements IGameFactory{
	private GTPDatabase db;
	private IGameService gameService;

	public GuessThePicGameFactory(IGameService gameService) {
		super();
		this.gameService = gameService;
		db = new GTPDatabase(gameService.getDatabase());
	}

	@Override
	public String getGameName() {
		return Values.GAME_GUESS_THE_PIC_NAME;
	}
	
	@Override
	public String getGameCommand() {
		return Values.GAME_GUESS_THE_PIC_COMMAND;
	}

	@Override
	public IGame createGame(MessageReceivedEvent event, List<String> args) {
		GuessThePicGame game = null;
		if(args.size() == 1){
			try{
				String listName = args.get(0);
				Long list_id = db.getListIdByNameAndGuild(event.getGuild().getLongID(), listName);
				game = new GuessThePicGame(gameService, event.getChannel(), event.getAuthor(), db, list_id);
			} catch (SQLException e){
				Logger.error("Could not create GTP game.");
				event.getChannel().sendMessage(Values.getMessageString(Values.MESSAGE_GTP_INVALID_LIST, args.get(0)));
				return null;
			}
		} else if (args.isEmpty()){
			game = new GuessThePicGame(gameService, event.getChannel(), event.getAuthor(), db, null);
		} else {
			return null;
		}
		
		game.addPlayer(event.getAuthor());
		return game;	
	}
	
	@Override
	public void initializeGameMode(){
			Keys.readKeys();
			db.updateWordDatabase();
	}

}
