package de.maxkroner.implementation;

import de.maxkroner.ui.BotMenue;
import de.maxkroner.ui.IBotMenue;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.shard.DisconnectedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.Image;
import sx.blah.discord.util.MessageBuilder;

public abstract class Bot {
	private String bot_name = "";
	protected BotMenue botMenue;

	protected IDiscordClient client; // The instance of the discord client.

	public Bot(String token, BotMenue botMenue2) {
		this.client = createClient(token);
		botMenue = botMenue2;
		EventDispatcher dispatcher = client.getDispatcher();
		dispatcher.registerListener(this); // BaseBot implements IListener
	}
	
	public void changeName(String name){
		client.changeUsername(name);
	}
	
	public void changePlayingText(String playingText){
		client.changePlayingText(playingText);
	}
	
	public void changeAvatar(String url, String imageType){
		client.changeAvatar(Image.forUrl(imageType, url));
	}

	public IDiscordClient getClient() {
		return this.client;
	}

	public void sendMessage(String message, IChannel channel, Boolean tts) {
			MessageBuilder mb = new MessageBuilder(this.client).withChannel(channel);
			if (tts)
				mb.withTTS();
			mb.withContent(message);
			mb.build();

	}

	@EventSubscriber
	public void onReady(ReadyEvent event) {
		bot_name = client.getOurUser().getName();
		botMenue.startMenue(this);
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