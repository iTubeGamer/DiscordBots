package de.maxkroner.gtp.runnable;


import java.util.concurrent.atomic.AtomicInteger;

import org.pmw.tinylog.Logger;

import de.maxkroner.gtp.database.Word;
import de.maxkroner.model.IGameService;
import de.maxkroner.values.Values;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.RequestBuffer;

public class DisplayNextImageRunnable<E> implements Runnable {
	private IGameService gameService;
	private IChannel channel;
	private Word word;
	private AtomicInteger image_count;
	private AtomicInteger hint_count;


	public DisplayNextImageRunnable(IGameService gameService, IChannel channel, Word word) {
		super();
		this.gameService = gameService;
		this.channel = channel;
		this.word = word;
		image_count = new AtomicInteger(0);	
		hint_count = new AtomicInteger(0);
	}

	@Override
	public void run() {		
		try{
			if(image_count.get() < Values.IMAGES_SHOWN_PER_ROUND){
				sendImage(word.getAndRemoveUrl());
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
				.append(word.getWord().substring(0, 1))
				.append(getStarsString(word.getWord().length() - 1))
				.toString();
	}
	
	private String getHint2(){
		return new StringBuilder()
					.append(getStarsString(word.getWord().length() - 1))
					.append(word.getWord().substring(word.getWord().length() -1, word.getWord().length()))
					.toString();
	}
	
	private String getHint3(){
		if(word.getWord().length() >= 5){
			return new StringBuilder()
					.append(getStarsString(2))
					.append(word.getWord().substring(2, word.getWord().length() - 2))
					.append(getStarsString(2))
					.toString();
		} else return word.getWord();
		
	}
	
	private static String getStarsString(int length){
		StringBuilder sb = new StringBuilder();
		for(int i = 1; i<=length; i++){
			sb.append("*");
		}
		return sb.toString();
	}

	
	
}
