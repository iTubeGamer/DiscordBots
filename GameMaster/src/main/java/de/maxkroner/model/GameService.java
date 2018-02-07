package de.maxkroner.model;

import sx.blah.discord.handle.obj.IChannel;

public interface GameService {
	public void sendMessage(String message, IChannel channel, boolean tts);
	
	public void gameStopped(IGame game);
}
