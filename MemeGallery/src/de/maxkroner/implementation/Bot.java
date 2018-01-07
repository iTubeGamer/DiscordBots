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
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.Image;
import sx.blah.discord.util.MessageBuilder;

public abstract class Bot {
	private String bot_name = "";
	protected BotMenue botMenue;

	private IDiscordClient client; // The instance of the discord client.

	public Bot(String token, BotMenue botMenue) {
		this.client = createClient(token);
		this.botMenue = botMenue;
		EventDispatcher dispatcher = client.getDispatcher();
		dispatcher.registerListener(this); // BaseBot implements IListener
	}
	
	public void changeName(String name){
		try {
			client.changeUsername(name);
			System.out.println("The botname was changed to \"" + name + "\"");
		} catch (Exception e) {
			System.out.println("Changing botname failed.");
			e.printStackTrace();
		}
	}
	
	public void changePlayingText(String playingText){
		try {
			client.changePlayingText(playingText);
			System.out.println("The playingText was changed to \"" + playingText + "\"");
		} catch (Exception e) {
			System.out.println("Changing playingText failed.");
			e.printStackTrace();
		}
	}
	
	public void changeAvatar(String url, String imageType){
		try {
			client.changeAvatar(Image.forUrl(imageType, url));
			System.out.println("The avatar has been successfully changed.");
		} catch (Exception e) {
			System.out.println("Chaning avatar failed.");
			e.printStackTrace();
		}
	}

	public IDiscordClient getClient() {
		return this.client;
	}

	protected void sendMessage(String message, IChannel channel, Boolean tts) {
		MessageBuilder mb = new MessageBuilder(this.client).withChannel(channel);
		if (tts)
			mb.withTTS();
		mb.withContent(message);
		mb.build();
	}
	
	protected void sendPrivateMessage(IUser recepient, String message) {
		MessageBuilder mb = new MessageBuilder(this.client).withChannel(recepient.getOrCreatePMChannel());
		mb.withContent(message);
		mb.build();
	}

	@EventSubscriber
	public void onReady(ReadyEvent event) {
		Bot bot = this;
		bot_name = client.getOurUser().getName();
		
		new Thread() {
			@Override
			public void run() {
				botMenue.startMenue(bot);
			}
		}.start();
		System.out.println("Logged in as " + bot_name);
	}

	@EventSubscriber
	protected void logout(DisconnectedEvent event) {
		System.out.println("Logged out for reason " + event.getReason() + "!");
	}
	
	public void disconnect(){
		try {
			client.logout();
			System.exit(0);
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