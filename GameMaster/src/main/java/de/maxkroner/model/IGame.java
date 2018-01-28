package de.maxkroner.model;

import java.util.Set;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

public interface IGame {
	
	public IChannel getChannel();
	
	public void setChannel(IChannel channel);
	
	public GameState getGameState();
	
	public void setGameState(GameState gameState);
	
	public String getName();
	
	public void addPlayer(IUser user);
	
	public Set<IUser> getPlayers();
	
	public int getRound();
	
	public void increaseRound();
	
	public int getPointsForPlayer(IUser player);
	
	public void setPointsForPlayer(IUser player, int points);

}
