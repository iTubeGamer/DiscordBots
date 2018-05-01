package de.maxkroner.gtp.implementation;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.pmw.tinylog.Logger;

import de.maxkroner.gtp.database.GTPDatabase;
import de.maxkroner.gtp.database.Word;
import de.maxkroner.gtp.runnable.DisplayNextImageRunnable;
import de.maxkroner.gtp.values.Keys;
import de.maxkroner.model.Game;
import de.maxkroner.model.IGameService;
import de.maxkroner.model.GameState;
import de.maxkroner.values.Values;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.RequestBuffer;

public class GuessThePicGame extends Game{
	private static ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(2);
	
	private GTPDatabase db;
	private Long list_id;
	private Word word;
	private String lastUrl ="";
	
	private ScheduledFuture<?> imageFuture;

	public GuessThePicGame(IGameService gameService, IChannel channel, IUser gameOwner, GTPDatabase db, Long list_id) {
		super(gameService, channel);
		setGameOwner(gameOwner);
		this.db = db;
		this.list_id = list_id;
		if(this.list_id == null){
			showLists();
		}		
	}


	@Override
	public void start() {
		if(list_id != null){
			setRound(1);
			startNewRound();
		} else {
			sendMessage(Values.MESSAGE_GTP_ERROR_NO_LIST_CHOSEN);
			showLists();
		}
	}
	

	@Override
	public void nextRound() {
		imageFuture.cancel(true);
		sendMessage(Values.getMessageString(Values.MESSAGE_GTP_SKIP_ROUND, String.valueOf(getRound()), word.getWord()));
		increaseRound();
		startNewRound();
	}
	
	@Override
	public void onMessageReceivedEvent(MessageReceivedEvent event) {
		String message = event.getMessage().getContent();
		if(getGameState().equals(GameState.RoundRunning) && 
				message.toLowerCase().equals(word.getWord().toLowerCase())){
			imageFuture.cancel(true);
			setGameState(GameState.RoundFinished);
			sendMessage(Values.getMessageString(Values.MESSAGE_GTP_RIGHT_GUESS, event.getAuthor().toString(), word.getWord()));
			increasePointsForPlayer(event.getAuthor());
			increaseRound();
			startNewRoundWithDelay(Values.ROUND_DELAY_IN_SECONDS);
			
		} else if(message.equals("log")){
			Logger.info(lastUrl);
		}
	}

	/**
	 * start the new round with a delay
	 * @param seconds delay in seconds
	 */
	private void startNewRoundWithDelay(int seconds) {
		scheduledExecutor.schedule(new Runnable(){
			@Override
			public void run() {
				startNewRound();
			}	
		}, seconds, TimeUnit.SECONDS);
	}

	private void startNewRound() {
		
		try {
			setGameState(GameState.RoundStarting);
			sendMessage(Values.getMessageString(Values.MESSAGE_GTP_NEW_ROUND, String.valueOf(getRound())));
			word = db.getRandomWordFromList(list_id, Values.IMAGES_SHOWN_PER_ROUND);
			DisplayNextImageRunnable<Object> displayNextImageRunnable = new DisplayNextImageRunnable<Object>(getGameService(), getChannel(), word);
			sendMessage(Values.MESSAGE_GTP_QUESTION);
			imageFuture = scheduledExecutor.scheduleAtFixedRate(displayNextImageRunnable, 0, Values.SHOW_IMAGE_DELAY, TimeUnit.SECONDS);
			setGameState(GameState.RoundRunning);
		} catch (SQLException e) {
			System.out.println(e);
			Logger.error(e);
			error();
		}
	}

	private void error() {
		sendMessage(Values.getMessageString(Values.MESSAGE_ERROR));
		stop();
		getGameService().gameStopped(this);	
	}

	@Override
	public void stop() {
		if(imageFuture != null){
			imageFuture.cancel(true);
		}
		printScoreBoard();
	}

	@Override
	public String getName() {
		return Values.GAME_GUESS_THE_PIC_NAME;
	}

	private void sendMessage(String message) {
		getGameService().sendMessage(message, getChannel(), false);
	}
	
	public void printScoreBoard(){
		EmbedBuilder builder = new EmbedBuilder();
		builder.withColor(184, 55, 53)
				.withTitle("Scoreboard Round #" + (getRound() - 1));
		for(IUser user : getPlayersSortedByScore()){
			builder.appendField(user.getName(), String.valueOf(getPointsForPlayer(user)), true);
		}
		
		RequestBuffer.request(() -> getChannel().sendMessage(builder.build()));
	}
	
	private void showLists(){
		sendMessage(Values.MESSAGE_GTP_CHOOSE_LIST);
		int counter = 1;
		for(String listName : db.getAllListsByGuild(getChannel().getGuild().getLongID())){
			sendMessage("(" +counter + ") " + listName);
			counter++;
		}
	}
}
