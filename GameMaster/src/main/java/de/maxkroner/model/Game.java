package de.maxkroner.model;

import java.util.HashMap;
import java.util.Set;

import sx.blah.discord.handle.obj.IUser;

public class Game {
	protected String name = "";
	protected GameState gameState;
	protected int round;
	protected HashMap<IUser, Integer> standings;
	
	public GameState getGameState() {
		return gameState;
	}
	
	public void setGameState(GameState gameState) {
		this.gameState = gameState;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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
	
	public void setRound(int round) {
		this.round = round;
	}
	
	public int getPointsForPlayer(IUser player){
		return standings.get(player);
	}

}
