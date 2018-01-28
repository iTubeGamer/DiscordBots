package de.maxkroner.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

public class Game implements IGame{
	private IChannel channel;
	private final String name;
	private GameState gameState;
	private int round;
	private Map<IUser, Integer> standings;
	
	public Game(String name, IChannel channel) {
		super();
		this.name = name;
		this.channel = channel;
		this.gameState = GameState.GameSetup;
		this.round = 0;
		this.standings = new HashMap<>();
	}

	public IChannel getChannel() {
		return channel;
	}

	public void setChannel(IChannel channel) {
		this.channel = channel;
	}

	public GameState getGameState() {
		return gameState;
	}
	
	public void setGameState(GameState gameState) {
		this.gameState = gameState;
	}
	
	public String getName() {
		return name;
	}
	
	public void addPlayer(IUser user){
		standings.put(user, 0);
	}
	
	public Set<IUser> getPlayers(){
		return standings.keySet();
	}
	
	public int getRound() {
		return round;
	}
	
	public void increaseRound() {
		this.round++;
	}
	
	public int getPointsForPlayer(IUser player){
		return standings.get(player);
	}
	
	public void setPointsForPlayer(IUser player, int points){
		if(standings.containsKey(player)){
			standings.put(player, points);
		}
	}

}
