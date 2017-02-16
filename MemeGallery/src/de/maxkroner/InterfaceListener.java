package de.maxkroner;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.obj.IUser;

public class InterfaceListener implements IListener<ReadyEvent> { // The event type in IListener<> can be any class which extends Event

	  @Override
	  public void handle(ReadyEvent event) { // This is called when the ReadyEvent is dispatched
		  IDiscordClient client = event.getClient(); // Gets the client from the event object
		  IUser ourUser = client.getOurUser();// Gets the user represented by the client
		  String name = ourUser.getName();// Gets the name of our user
		  System.out.println("Logged in as " + name);
	  }
	}