package de.maxkroner.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import de.maxkroner.util.MapUtil;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

public abstract class Game implements IGame{
	private IGameService gameService;
	private IChannel channel;
	private IUser gameOwner;
	private GameState gameState;
	private int round;
	private Map<IUser, Integer> standings;
	
	public Game(IGameService gameService, IChannel channel) {
		super();
		this.gameService = gameService;
		this.channel = channel;
		this.gameState = GameState.GameSetup;
		this.round = 0;
		this.standings = new HashMap<>();
		this.gameOwner = null;
	}
	
	public Game(IChannel channel, IUser gameOwner, IGameService gameService) {
		this(gameService, channel);
		this.gameOwner = gameOwner;
	}
	
	protected IGameService getGameService(){
		return gameService;
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
	
	public List<IUser> getPlayersSortedByScore(){
		return MapUtil.sortByValue(standings).keySet().stream().collect(Collectors.toList());
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
