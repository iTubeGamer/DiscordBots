package de.maxkroner.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.pmw.tinylog.Logger;

import de.maxkroner.implementation.runnable.DisplayNextImageRunnable;
import de.maxkroner.values.Values;
import reader.ImageUrlReader;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.RequestBuffer;

public class GuessThePicGame extends Game{
	private static final List<String> wordList = new ArrayList<>();
	private static final int SHOW_IMAGE_DELAY = 20;
	
	private String word;
	private static ScheduledExecutorService scheduledExecutorService;
	private ScheduledFuture<?> imageFuture;

	public GuessThePicGame(IChannel channel, IUser gameOwner) {
		super(channel);
		setGameOwner(gameOwner);
		readWordList();
		scheduledExecutorService = Executors.newScheduledThreadPool(2);
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
		if(getGameState().equals(GameState.RoundRunning) && 
				event.getMessage().getContent().toLowerCase().equals(word.toLowerCase())){
			imageFuture.cancel(true);
			setGameState(GameState.RoundFinished);
			sendMessage(Values.getMessageString(Values.MESSAGE_GTP_RIGHT_GUESS, event.getAuthor().toString(), word));
			increasePointsForPlayer(event.getAuthor());
			increaseRound();
			startNewRound();
		}
	}


	private void startNewRound() {
		setGameState(GameState.RoundStarting);
		sendMessage(Values.getMessageString(Values.MESSAGE_GTP_NEW_ROUND, String.valueOf(getRound())));
		word = getRandomWord();
		List<String> urls = ImageUrlReader.getImageUrlsForTerm(word);
		if(!urls.isEmpty()){
			DisplayNextImageRunnable<Object> displayNextImageRunnable = new DisplayNextImageRunnable<Object>(getClient(), getChannel(), urls, word);
			sendMessage(Values.MESSAGE_GTP_QUESTION);
			imageFuture = scheduledExecutorService.scheduleAtFixedRate(displayNextImageRunnable, 0, SHOW_IMAGE_DELAY, TimeUnit.SECONDS);
			setGameState(GameState.RoundRunning);
		} else {
			sendMessage(Values.MESSAGE_ERROR_IMAGE_SERVICE);
		}
		
	}

	private String getRandomWord() {
		int index = ThreadLocalRandom.current().nextInt(0, wordList.size());
		return wordList.get(index);
	}

	@Override
	public void stop() {
		imageFuture.cancel(true);
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
	
	protected void sendMessage(String message) {
		MessageBuilder mb = new MessageBuilder(getClient()).withChannel(getChannel());
		mb.withContent(message);
		mb.build();
	}
	
	public void printScoreBoard(){
		EmbedBuilder builder = new EmbedBuilder();
		builder.withColor(184, 55, 53)
				.withTitle("Scoreboard");
		for(IUser user : getPlayers()){
			builder.appendField(user.getName(), String.valueOf(getPointsForPlayer(user)), false);
		}
		
		RequestBuffer.request(() -> getChannel().sendMessage(builder.build()));
	}




}
