package de.maxkroner.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.pmw.tinylog.Logger;

import de.maxkroner.implementation.runnable.DisplayNextImageRunnable;
import de.maxkroner.reader.ImageUrlReader;
import de.maxkroner.values.Values;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.RequestBuffer;

public class GuessThePicGame extends Game{
	public static String lastUrl ="";
	private static final List<String> wordList = new ArrayList<>();
	private static final int SHOW_IMAGE_DELAY = 20;
	private List<String> urls;
	
	private String word;
	private static ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(2);
	private ScheduledFuture<?> imageFuture;

	public GuessThePicGame(GameService gameService, IChannel channel, IUser gameOwner) {
		super(gameService, channel);
		setGameOwner(gameOwner);
		readWordList();
	}

	@Override
	public void start() {
		setRound(1);
		startNewRound();
	}
	

	@Override
	public void nextRound() {
		imageFuture.cancel(true);
		sendMessage(Values.getMessageString(Values.MESSAGE_GTP_SKIP_ROUND, String.valueOf(getRound()), word));
		increaseRound();
		startNewRound();
	}
	
	@Override
	public void onMessageReceivedEvent(MessageReceivedEvent event) {
		String message = event.getMessage().getContent();
		if(getGameState().equals(GameState.RoundRunning) && 
				message.toLowerCase().equals(word.toLowerCase())){
			imageFuture.cancel(true);
			setGameState(GameState.RoundFinished);
			sendMessage(Values.getMessageString(Values.MESSAGE_GTP_RIGHT_GUESS, event.getAuthor().toString(), word));
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
		setGameState(GameState.RoundStarting);
		sendMessage(Values.getMessageString(Values.MESSAGE_GTP_NEW_ROUND, String.valueOf(getRound())));
		word = getRandomWord();
		urls = ImageUrlReader.getImageUrlsForTerm(word, ThreadLocalRandom.current().nextInt(0, 20));
		if(!urls.isEmpty()){
			DisplayNextImageRunnable<Object> displayNextImageRunnable = new DisplayNextImageRunnable<Object>(getGameService(), getChannel(), urls, word);
			sendMessage(Values.MESSAGE_GTP_QUESTION);
			imageFuture = scheduledExecutor.scheduleAtFixedRate(displayNextImageRunnable, 0, SHOW_IMAGE_DELAY, TimeUnit.SECONDS);
			setGameState(GameState.RoundRunning);
		} else {
			sendMessage(Values.MESSAGE_ERROR_IMAGE_SERVICE);
			stop();
			getGameService().gameStopped(this);
		}	
	}

	private String getRandomWord() {
		int index = ThreadLocalRandom.current().nextInt(0, wordList.size());
		return wordList.get(index);
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
	

	private void readWordList(){
		String line = "";
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		InputStream is = classloader.getResourceAsStream("wordlist.txt");

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			if (is != null) {
				while ((line = reader.readLine()) != null) {
					wordList.add(line);
				}
			}
		} catch (IOException e) {
			Logger.error(e);
		}
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
	
	private void updateDatabase(){
		//TODO
		//read txt lists
		//check if list exists in db -> else create list
		//for every word in list check if word exists in db -> else create word for list in db
	}
}
