package de.maxkroner.model;

import de.maxkroner.db.GameMasterDatabase;
import sx.blah.discord.handle.obj.IChannel;

public interface IGameService {
	public void sendMessage(String message, IChannel channel, boolean tts);
	
	public void gameStopped(IGame game);
	
	public GameMasterDatabase getDatabase();
}
