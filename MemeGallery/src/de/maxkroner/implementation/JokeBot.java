package de.maxkroner.implementation;
import java.sql.SQLException;

import de.maxkroner.main.JokeDatabase;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.obj.User;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;


public class JokeBot extends BaseBot{
	private static final String bot_name = "Witzbold";
	private static final String bot_status = "Lachflash";
	private static final String profile_image_url = "https://s-media-cache-ak0.pinimg.com/736x/b6/cc/b0/b6ccb09b0cc1de2b2d491798f870ab6d.jpg";
	private static final String profile_image_imageType = "jpeg";
	
	public JokeBot(String token){
		super(token, bot_name, bot_status, profile_image_url, profile_image_imageType);
	}
	
	private void tellHitlerjoke(IChannel channel){
		User user = (User) client.getOurUser();
		try {
			channel.getGuild().setUserNickname(user, "Hitler");
			new MessageBuilder(this.client).withChannel(channel).withContent("Immer diese scheiﬂ Gasrechnung!").withTTS().build();
		} catch (DiscordException | RateLimitException | MissingPermissionsException e) {
			e.printStackTrace();
		}	
	}
	
	private void tellFlachwitz(IChannel channel) throws MissingPermissionsException, DiscordException, RateLimitException, ClassNotFoundException, SQLException{
		IGuild guild = channel.getGuild();
		if (!client.getOurUser().getNicknameForGuild(guild).orElse("").equals(bot_name)){
				guild.setUserNickname(client.getOurUser(), bot_name);
		}
		//get random joke
		String joke = JokeDatabase.getRandomJoke("flach");
		
		MessageBuilder mb = new MessageBuilder(this.client).withChannel(channel).withTTS();
		mb.withContent(joke);
		mb.build();
	}
	

	@EventSubscriber
	public void onReady(ReadyEvent event) {
		super.onReady(event);
		for (IGuild guild : client.getGuilds()){
			if (!client.getOurUser().getNicknameForGuild(guild).orElse("").equals(bot_name)){
				try {
					guild.setUserNickname(client.getOurUser(), bot_name);
				} catch (MissingPermissionsException | DiscordException | RateLimitException e) {
					e.printStackTrace();
				}
			}	
		}
	}
	
	@EventSubscriber
	public void onMessageReceivedEvent(MessageReceivedEvent event) throws MissingPermissionsException, DiscordException, RateLimitException, ClassNotFoundException, SQLException {
		String message = event.getMessage().getContent();
		IChannel channel = event.getMessage().getChannel();
		if (message.startsWith("!change nick")){
			User user = (User) client.getOurUser();
			channel.getGuild().setUserNickname(user, message.substring(13));
		} else if (message.equals("!joke hitler")){
			tellHitlerjoke(channel);
		} else if (message.equals("!joke flach")){
			tellFlachwitz(channel);
		}
		
	}

}
