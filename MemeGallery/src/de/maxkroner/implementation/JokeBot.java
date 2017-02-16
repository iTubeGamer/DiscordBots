package de.maxkroner.implementation;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.Image;
import sx.blah.discord.util.RateLimitException;


public class JokeBot extends BaseBot {
	private static final String hitler_url="https://steamcdn-a.akamaihd.net/steamcommunity/public/images/avatars/e1/e1c65d74fea107775d952de555f24eb3370fef45_full.jpg";
	private static final String bot_name = "JokeBot";
	private static final String profile_image_url = "https://s-media-cache-ak0.pinimg.com/736x/b6/cc/b0/b6ccb09b0cc1de2b2d491798f870ab6d.jpg";
	private static final String profile_image_imageType = "jpeg";
	
	public JokeBot(String token){
		super(token, bot_name, profile_image_url, profile_image_imageType);
	}
	
	private void tellHitlerjoke(){
		try {
			client.changeUsername("Hitler");
			client.changeAvatar(Image.forUrl("jpeg", hitler_url));
		} catch (DiscordException | RateLimitException e) {
			e.printStackTrace();
		}	
	}
	
	@Override
	public void handle(ReadyEvent event) {
		super.handle(event);
	}

}
