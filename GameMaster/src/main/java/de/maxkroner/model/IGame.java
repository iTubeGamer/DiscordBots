package de.maxkroner.model;

import java.util.Set;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

public interface IGame {
	
	public void start();
	
	public void nextRound();
	
	public void stop();
	
	public void printScoreBoard();
	
	public void onMessageReceivedEvent(MessageReceivedEvent event);
	
	public IChannel getChannel();
	
	public IUser getGameOwner();
	
	public void setGameOwner(IUser gameOwner);
	
	public GameState getGameState();
	
	public String getName();
	
	public void addPlayer(IUser user);
	
	public void removePlayer(IUser user);
	
	public Set<IUser> getPlayers();

}
