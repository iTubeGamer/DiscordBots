package de.maxkroner.implementation;

import de.maxkroner.model.IBot;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;


public class BaseBot implements IListener<ReadyEvent>, IBot{
	
	protected IDiscordClient client; // The instance of the discord client.

	public BaseBot(String token) {
		this.client = createClient(token);
		EventDispatcher dispatcher = client.getDispatcher();
		dispatcher.registerListener(this); //BaseBot implements IListener
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
		IDiscordClient client = event.getClient(); // Gets the client from the event object
		IUser ourUser = client.getOurUser();// Gets the user represented by the client
		String name = ourUser.getName();// Gets the name of our user
		System.out.println("Logged in as " + name);
	}

}