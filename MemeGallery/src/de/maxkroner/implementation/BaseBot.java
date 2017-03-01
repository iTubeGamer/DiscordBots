package de.maxkroner.implementation;

import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.DisconnectedEvent;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.Status;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.Image;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

public abstract class BaseBot {
	protected String bot_name = "BaseBot";
	protected String bot_status = "This is the BaseBot description.";
	protected String profile_image_url = "http://i.imgur.com/LxbiFqo.jpg";
	protected String profile_image_imageType = "jpeg";

	protected IDiscordClient client; // The instance of the discord client.

	public BaseBot(String token) {
		this.client = createClient(token);
		EventDispatcher dispatcher = client.getDispatcher();
		dispatcher.registerListener(this); // BaseBot implements IListener
	}

	public BaseBot(String token, String bot_name, String bot_status, String profile_image_url,
			String profile_image_imageType) {
		this(token);
		this.bot_name = bot_name;
		this.bot_status = bot_status;
		this.profile_image_imageType = profile_image_imageType;
		this.profile_image_url = profile_image_url;
	}

	public IDiscordClient getClient() {
		return this.client;
	}

	public String getBotName() {
		return this.bot_name;
	}

	public String getImageUrl() {
		return this.profile_image_imageType;
	}

	public void sendMessage(String message, IChannel channel, Boolean tts) {
		try {
			MessageBuilder mb = new MessageBuilder(this.client).withChannel(channel);
			if (tts)
				mb.withTTS();
			mb.withContent(message);
			mb.build();
		} catch (RateLimitException | DiscordException | MissingPermissionsException e) {
			e.printStackTrace();
		}
	}

	public void resetNickname(IGuild guild) {
		if (!client.getOurUser().getNicknameForGuild(guild).orElse("").equals(bot_name)) {
			try {
				guild.setUserNickname(client.getOurUser(), bot_name);
			} catch (MissingPermissionsException | DiscordException | RateLimitException e) {
				e.printStackTrace();
			}
		}
	}

	@EventSubscriber
	public void onReady(ReadyEvent event) {
		try {
			client.changeUsername(this.bot_name);
			client.changeStatus(Status.game(bot_status));
			client.changeAvatar(Image.forUrl(profile_image_imageType, profile_image_url));
		} catch (DiscordException | RateLimitException e) {
			e.printStackTrace();
		}

		System.out.println("Logged in as " + bot_name);
	}

	@EventSubscriber
	public void logout(DisconnectedEvent event) {
		System.out.println("Logged out for reason " + event.getReason() + "!");
	}
	
	public void disconnect(){
		try {
			client.logout();
		} catch (DiscordException e) {
			e.printStackTrace();
		}
	}

	public static IDiscordClient createClient(String token) {
		ClientBuilder clientBuilder = new ClientBuilder();
		clientBuilder.withToken(token); // Adds the login info to the builder
		try {
			return clientBuilder.login(); // Creates the client instance and
											// logs the client in
		} catch (DiscordException e) {
			e.printStackTrace();
			return null;
		}
	}

}