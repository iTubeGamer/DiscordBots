package de.maxkroner.implementation.runnable;


import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import org.pmw.tinylog.Logger;

import de.maxkroner.model.GameService;
import de.maxkroner.model.GuessThePicGame;
import de.maxkroner.values.Values;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.RequestBuffer;

public class DisplayNextImageRunnable<E> implements Runnable {
	private GameService gameService;
	private IChannel channel;
	private List<String> urls;
	private String word;
	private AtomicInteger image_count;
	private AtomicInteger hint_count;


	public DisplayNextImageRunnable(GameService gameService, IChannel channel, List<String> urls, String word) {
		super();
		this.gameService = gameService;
		this.channel = channel;
		this.urls = urls;
		this.word = word;
		image_count = new AtomicInteger(0);	
		hint_count = new AtomicInteger(0);
	}

	@Override
	public void run() {		
		try{
			if(image_count.get() < Values.MAX_IMAGES_SHOWN && image_count.get() < urls.size()){
				sendImage(getRandomUrl());
				image_count.getAndIncrement();
			} else if(hint_count.get() == 0) {
				sendMessage(Values.getMessageString(Values.MESSAGE_GTP_HINT_1, getHint1()));
				hint_count.getAndIncrement();
			} else if(hint_count.get() == 1){
				sendMessage(Values.getMessageString(Values.MESSAGE_GTP_HINT_1, getHint2()));
				hint_count.getAndIncrement();
			} else if(hint_count.get() == 2){
				sendMessage(Values.getMessageString(Values.MESSAGE_GTP_HINT_1, getHint3()));
				hint_count.getAndIncrement();
			}
		} catch(Exception e){
			Logger.error(e);
		}
		
	}
	
	private String getRandomUrl(){
		int index = ThreadLocalRandom.current().nextInt(0, urls.size());
		String url = urls.get(index);
		GuessThePicGame.lastUrl = url;
		urls.remove(index);
		return url;
	}
	
	private void sendMessage(String message) {
		gameService.sendMessage(message, channel, false);
		
	}
	
	private void sendImage(String url){
		EmbedBuilder builder = new EmbedBuilder();
		builder.withColor(184, 55, 53)
				.withTitle("Image #" + (image_count.get() + 1))
				.withImage(url);

		RequestBuffer.request(() -> channel.sendMessage(builder.build()));
	}
	
	
	private String getHint1(){
		return new StringBuilder()
				.append(word.substring(0, 1))
				.append(getStarsString(word.length() - 1))
				.toString();
	}
	
	private String getHint2(){
		return new StringBuilder()
					.append(getStarsString(word.length() - 1))
					.append(word.substring(word.length() -1, word.length()))
					.toString();
	}
	
	private String getHint3(){
		if(word.length() >= 5){
			return new StringBuilder()
					.append(getStarsString(2))
					.append(word.substring(2, word.length() - 2))
					.append(getStarsString(2))
					.toString();
		} else return word;
		
	}
	
	private static String getStarsString(int length){
		StringBuilder sb = new StringBuilder();
		for(int i = 1; i<=length; i++){
			sb.append("*");
		}
		return sb.toString();
	}

	
	
}
