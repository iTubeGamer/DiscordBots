package de.maxkroner.implementation.privateBot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

public class TempChannelMap {
	private HashMap<IChannel, TempChannel> channelTempChannelMap;
	private HashMap<IUser, ArrayList<TempChannel>> userTempChannelMap;
	
	public TempChannelMap() {
		this.channelTempChannelMap = new HashMap<IChannel, TempChannel>();
		this.userTempChannelMap = new HashMap<IUser, ArrayList<TempChannel>>();
	}
	
	public void addTempChannel(TempChannel tempChannel){
		//fill both Maps
		channelTempChannelMap.put(tempChannel.getChannel(), tempChannel);
		//create new ArrayList for User, if doesnt exists
		if(!userTempChannelMap.containsKey(tempChannel.getOwner())){
			userTempChannelMap.put(tempChannel.getOwner(), new ArrayList<TempChannel>());
		}
		userTempChannelMap.get(tempChannel.getOwner()).add(tempChannel);	
	}
	
	public void removeTempChannel(TempChannel tempChannel){
		//remove from both Maps
		channelTempChannelMap.remove(tempChannel.getChannel());
		userTempChannelMap.get(tempChannel.getOwner()).remove(tempChannel);
	}
	
	public boolean tempChannelForChannelExists(IChannel channel){
		return channelTempChannelMap.containsKey(channel);
	}
	
 	public TempChannel getTempChannelForChannel(IChannel channel){
		return channelTempChannelMap.get(channel);
	}
	
	public ArrayList<TempChannel> getTempChannelListForUser(IUser user){
		if (userTempChannelMap.containsKey(user)){
			return userTempChannelMap.get(user);
		}
		
		return new ArrayList<TempChannel>();
		
	}
	
	public Collection<TempChannel> getAllTempChannel(){
		return channelTempChannelMap.values();
	}
	
	public int getUserChannelCount(IUser user){
		if(!userTempChannelMap.containsKey(user)){
			return 0;
		}
		return getTempChannelListForUser(user).size();
	}
	
}
