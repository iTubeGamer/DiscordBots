package de.maxkroner.implementation.privateBot;

import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ScheduledExecutorService;

import sx.blah.discord.handle.obj.IGuild;

public class CheckTempChannel<E> implements Runnable {
	private HashMap<IGuild, TempChannelMap> channelMap;
	
	public CheckTempChannel(HashMap<IGuild, TempChannelMap> channelMap, ScheduledExecutorService executor) {
		super();
		this.channelMap = channelMap;
	}
	
	@Override
	public void run() {
		//iterate over the tempChannelMaps per Guild
		for(TempChannelMap tempChannelMap : channelMap.values()){
			//iterate over all the tempChannels of the Guild (use Iterator.hasNext()) as Elements get deleted while iterating)
			for(Iterator<TempChannel> iterator = tempChannelMap.getAllTempChannel().iterator(); iterator.hasNext();){
				TempChannel tempChannel = iterator.next();
				//if Channel still exists
				if(!tempChannel.getChannel().isDeleted()){
					if (tempChannel.getChannel().getConnectedUsers().isEmpty()){
						tempChannel.setEmptyMinuts(tempChannel.getEmptyMinuts() + 1);
						if(tempChannel.getEmptyMinuts() >= tempChannel.getTimeoutInMinutes()){
							tempChannel.getChannel().delete();
						}
					} else {
						tempChannel.setEmptyMinuts(0);
					}
				//if Channel was already deleted
				} else {
					tempChannelMap.removeTempChannel(tempChannel);
				}
				
			}
		}
	}

}
