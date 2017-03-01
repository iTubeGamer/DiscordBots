package de.maxkroner.implementation;

import java.sql.SQLException;
import java.util.List;

import de.maxkroner.ui.ConsoleMenue;
import die.maxkroner.database.JokeDatabase;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.obj.User;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

public class JokeBot extends BaseBot {
	private static final String bot_name = "Witzbold";
	private static final String bot_status = "Lachflash";
	private static final String profile_image_url = "https://s-media-cache-ak0.pinimg.com/736x/b6/cc/b0/b6ccb09b0cc1de2b2d491798f870ab6d.jpg";
	private static final String profile_image_imageType = "jpeg";
	private List<String> jokeCategories;
	private ConsoleMenue consoleMenue;

	public JokeBot(String token, ConsoleMenue consoleMenue) {
		super(token, bot_name, bot_status, profile_image_url, profile_image_imageType);
		this.consoleMenue = consoleMenue;
	}

	private void tellJoke(String category, IChannel channel) {
		if (jokeCategories.contains(category)) {

			String joke = JokeDatabase.getRandomJoke(category);
			sendMessage(joke, channel, true);

		} else {
			// Todo: I don't know this category yet. Train me by...
		}
	}

	@EventSubscriber
	public void onReady(ReadyEvent event) {
		super.onReady(event);
		updateJokeCategories();
		consoleMenue.startMenue(this);
	}

	public void updateJokeCategories() {
		jokeCategories = JokeDatabase.getJokeCategories();
		
	}

	@EventSubscriber
	public void onMessageReceivedEvent(MessageReceivedEvent event) throws MissingPermissionsException, DiscordException,
			RateLimitException, ClassNotFoundException, SQLException {
		String message = event.getMessage().getContent();
		IChannel channel = event.getMessage().getChannel();
		if (message.startsWith("!change nick")) {
			User user = (User) client.getOurUser();
			channel.getGuild().setUserNickname(user, message.substring(13));
		} else if (message.startsWith("!joke ")) {
			tellJoke(message.substring(message.indexOf(" ") + 1), channel);
		}
	}

}
