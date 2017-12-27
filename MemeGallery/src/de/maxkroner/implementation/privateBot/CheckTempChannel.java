package de.maxkroner.implementation.privateBot;

import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ScheduledExecutorService;

import org.pmw.tinylog.Logger;

import sx.blah.discord.handle.obj.IGuild;

public class CheckTempChannel<E> implements Runnable {
	private HashMap<IGuild, TempChannelMap> channelMap;
	
	public CheckTempChannel(HashMap<IGuild, TempChannelMap> channelMap, ScheduledExecutorService executor) {
		super();
		this.channelMap = channelMap;
	}
	
	@Override
	public void run() {
		Logger.info("checking timeout of TempChannels");
		//iterate over the tempChannelMaps per Guild
		for(TempChannelMap tempChannelMap : channelMap.values()){
			//iterate over all the tempChannels of the Guild (use Iterator.hasNext()) as Elements get deleted while iterating)
			for(Iterator<TempChannel> iterator = tempChannelMap.getAllTempChannel().iterator(); iterator.hasNext();){
				TempChannel tempChannel = iterator.next();
				//if Channel still exists
				if(!tempChannel.getChannel().isDeleted()){
					if (tempChannel.getChannel().getConnectedUsers().isEmpty()){
						Logger.info("Channel \"{}\" is empty, increasing emptyMinutes to", tempChannel.getChannel().getName(), tempChannel.getEmptyMinuts());
						tempChannel.setEmptyMinutes(tempChannel.getEmptyMinuts() + 1);
						if(tempChannel.getEmptyMinuts() >= tempChannel.getTimeoutInMinutes()){
							Logger.info("Channel \"{}\" reached it's timeout, deleting channel now", tempChannel.getChannel().getName());
							tempChannel.getChannel().delete();
							tempChannelMap.removeTempChannel(tempChannel);
						}
					} else {
						Logger.info("Channel \"{}\" wasn't empty, setting timeout to 0", tempChannel.getChannel().getName());
						tempChannel.setEmptyMinutes(0);
					}
				//if Channel was already deleted
				} else {
					Logger.warn("Channel \"{}\" in ChannelMap didn't exist anymore!", tempChannel.getChannel().getName());
					tempChannelMap.removeTempChannel(tempChannel);
				}
				
			}
		}
	}

}
