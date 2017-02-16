package de.maxkroner.implementation;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.Image;
import sx.blah.discord.util.RateLimitException;

import java.net.URL;

import org.apache.commons.io.FileUtils;

public class JokeBot extends BaseBot {
	private static final String hitler_url="https://steamcdn-a.akamaihd.net/steamcommunity/public/images/avatars/e1/e1c65d74fea107775d952de555f24eb3370fef45_full.jpg";
	public JokeBot(String token){
		super(token);
	}
	
	private void tellHitlerjoke(){
		//change profile picture to hitler picture
		try {
			client.changeAvatar(Image.forUrl("jpeg", hitler_url));
		} catch (DiscordException | RateLimitException e) {
			e.printStackTrace();
		}
		
		
	}

}
