package de.maxkroner.implementation;

import de.maxkroner.model.IBot;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.Image;
import sx.blah.discord.util.RateLimitException;


public abstract class BaseBot implements IListener<ReadyEvent>, IBot{
	protected String bot_name = "BaseBot";
	protected String profile_image_url = "http://i.imgur.com/LxbiFqo.jpg";
	protected String profile_image_imageType = "jpeg";
	
	protected IDiscordClient client; // The instance of the discord client.

	public BaseBot(String token) {
		this.client = createClient(token);
		EventDispatcher dispatcher = client.getDispatcher();
		dispatcher.registerListener(this); //BaseBot implements IListener
	}
	
	public BaseBot(String token, String bot_name, String profile_image_url, String profile_image_imageType) {
		this(token);
		this.bot_name = bot_name;
		this.profile_image_imageType = profile_image_imageType;
		this.profile_image_url = profile_image_url;
	}
	
	public IDiscordClient getClient(){
		return this.client;
	}

	public static IDiscordClient createClient(String token) { // Returns a new instance of the Discord client
	    ClientBuilder clientBuilder = new ClientBuilder(); // Creates the ClientBuilder instance
	    clientBuilder.withToken(token); // Adds the login info to the builder
	    try {
	           return clientBuilder.login(); // Creates the client instance and logs the client in
	    } catch (DiscordException e) { // This is thrown if there was a problem building the client
	        e.printStackTrace();
	        return null;
	    }
	  }
	
	@Override
	public void handle(ReadyEvent event) {
		
		try {
			client.changeUsername(this.bot_name);
			client.changeAvatar(Image.forUrl(profile_image_imageType, profile_image_url));
		} catch (DiscordException | RateLimitException e) {
			e.printStackTrace();
		}	
		
		System.out.println("Logged in as " + bot_name);
	}

}