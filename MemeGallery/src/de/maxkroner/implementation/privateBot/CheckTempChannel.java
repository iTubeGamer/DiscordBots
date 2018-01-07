package de.maxkroner.implementation.privateBot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
		try{
			//iterate over the tempChannelMaps per Guild
			for(TempChannelMap tempChannelMap : channelMap.values()){
				//iterate over all the tempChannels of the Guild (collect all TempChannels and delete them later)
				List<TempChannel> tempChannelToDelete = new ArrayList<TempChannel>();
				for(TempChannel tempChannel : tempChannelMap.getAllTempChannel()){
					//if Channel still exists
					if(!tempChannel.getChannel().isDeleted()){
						if (tempChannel.getChannel().getConnectedUsers().isEmpty()){
							tempChannel.setEmptyMinutes(tempChannel.getEmptyMinuts() + 1);
							Logger.info("Channel \"{}\" is empty, increased emptyMinutes to {}", tempChannel.getChannel().getName(), tempChannel.getEmptyMinuts());
							if(tempChannel.getEmptyMinuts() > tempChannel.getTimeoutInMinutes()){
								Logger.info("Channel \"{}\" exceeded it's timeout of {} minutes, deleting channel now", tempChannel.getChannel().getName(), tempChannel.getTimeoutInMinutes());
								tempChannelToDelete.add(tempChannel);
							}
						} else {
							tempChannel.setEmptyMinutes(0);
							Logger.info("Channel \"{}\" wasn't empty, timeout set to 0", tempChannel.getChannel().getName());
						}
					//if Channel was already deleted
					} else {
						tempChannelToDelete.add(tempChannel);
						Logger.warn("Channel \"{}\" in ChannelMap didn't exist anymore, removed it now!", tempChannel.getChannel().getName());
					}		
				}
				
				for(TempChannel tempChannel : tempChannelToDelete){
					//delete the VoiceChannel only, the TempChannel will be removed in onVoiceChannelDeleteEvent
					tempChannel.getChannel().delete();
				}
			}
		} catch (Exception e){
			Logger.error(e);
		}
	}

}
