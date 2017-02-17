package de.maxkroner.model;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.DisconnectedEvent;
import sx.blah.discord.handle.impl.events.ReadyEvent;


public interface IBot{
	
	public IDiscordClient getClient();
	public String getBotName();
	public String getImageUrl();
	public void logout(DisconnectedEvent event);
	public void onReady(ReadyEvent event);
}
