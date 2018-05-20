package de.maxkroner.implementation;

import sx.blah.discord.handle.obj.IChannel;

public interface IClientService {
	
	public void sendMessage(String message, IChannel channel, boolean tts);

}
