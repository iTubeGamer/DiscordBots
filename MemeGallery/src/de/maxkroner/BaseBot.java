package de.maxkroner;

import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.util.DiscordException;

/**
 * This represents a SUPER basic bot (literally all it does is login).
 * This is used as a base for all example bots.
 */
public class BaseBot {
	
	private IDiscordClient client; // The instance of the discord client.
	

	public BaseBot(String token) {
		this.client = createClient(token);
		EventDispatcher dispatcher = client.getDispatcher();
		dispatcher.registerListener(new InterfaceListener());
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

}