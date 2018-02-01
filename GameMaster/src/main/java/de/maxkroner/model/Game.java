package de.maxkroner.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

public abstract class Game implements IGame{
	private IDiscordClient client;
	private IChannel channel;
	private IUser gameOwner;
	private GameState gameState;
	private int round;
	private Map<IUser, Integer> standings;
	
	public Game(IChannel channel) {
		super();
		this.channel = channel;
		this.gameState = GameState.GameSetup;
		this.round = 0;
		this.standings = new HashMap<>();
		this.gameOwner = null;
	}
	
	public Game(IChannel channel, IUser gameOwner, IDiscordClient client) {
		this(channel);
		this.gameOwner = gameOwner;
		this.client = client;
	}
	
	protected IDiscordClient getClient(){
		return client;
	}

	public IChannel getChannel() {
		return channel;
	}
	
	public IUser getGameOwner(){
		return gameOwner;
	}
	
	public void setGameOwner(IUser gameOwner){
		this.gameOwner = gameOwner;
	}

	public GameState getGameState() {
		return gameState;
	}
	
	protected void setGameState(GameState gameState){
		this.gameState = gameState;
	}
	
	public void addPlayer(IUser user){
		standings.put(user, 0);
	}
	
	public void removePlayer(IUser player){
		standings.remove(player);
	}
	
	public Set<IUser> getPlayers(){
		return standings.keySet();
	}
	
	public int getRound() {
		return round;
	}
	
	protected void setRound(int round) {
		this.round = round;
	}
	
	protected void increaseRound() {
		this.round++;
	}
	
	protected int getPointsForPlayer(IUser player){
		return standings.get(player);
	}
	
	protected void setPointsForPlayer(IUser player, int points){
		if(standings.containsKey(player)){
			standings.put(player, points);
		}
	}
	
	protected void increasePointsForPlayer(IUser player){
		if(standings.containsKey(player)){
			standings.put(player, standings.get(player) + 1);
		}
	}

}
