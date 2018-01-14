package de.maxkroner.implementation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import org.pmw.tinylog.Logger;

import de.maxkroner.model.TempChannel;
import de.maxkroner.model.TempChannelMap;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MessageBuilder;

public class CheckTempChannelRunnable<E> implements Runnable {
	private HashMap<IGuild, TempChannelMap> tempChannelsByGuild;
	IDiscordClient client;

	public CheckTempChannelRunnable(HashMap<IGuild, TempChannelMap> channelMap, IDiscordClient client, ScheduledExecutorService executor) {
		super();
		this.client = client;
		this.tempChannelsByGuild = channelMap;
	}

	public void run() {
		try {
			//collect all TempChannels and delete them later
			List<TempChannel> tempChannelToDelete = new ArrayList<TempChannel>();
			
			// iterate over the tempChannelMaps per Guild to check the timeout
			for (TempChannelMap tempChannelMap : tempChannelsByGuild.values()) {
				// iterate over all the tempChannels of the Guild 
				for (TempChannel tempChannel : tempChannelMap.getAllTempChannel()) {
					// if Channel still exists
					if (!tempChannel.getChannel().isDeleted()) {
						updateTempChannelAndSendNotifications(tempChannelToDelete, tempChannel);
					} else if(tempChannel.getChannel().isDeleted() && tempChannel.getEmptyMinutes() != 0) {
						// if Channel was already deleted (shouldn't happen)
						tempChannelToDelete.add(tempChannel);
						Logger.warn("Channel \"{}\" in ChannelMap didn't exist anymore, removed it now!", tempChannel.getChannel().getName());
					}
				}	
			}
			
			for (TempChannel tempChannel : tempChannelToDelete) {
				// delete the VoiceChannel only, the TempChannel will be removed in onVoiceChannelDeleteEvent
				tempChannel.getChannel().delete();
			}
			
		} catch (Exception e) {
			Logger.error(e);
		}
	}

	private void updateTempChannelAndSendNotifications(List<TempChannel> tempChannelToDelete, TempChannel tempChannel) {
		if (tempChannel.getChannel().getConnectedUsers().isEmpty()) {
			tempChannel.setEmptyMinutes(tempChannel.getEmptyMinutes() + 1);
			Logger.info("Channel \"{}\" is empty, increased emptyMinutes to {}", tempChannel.getChannel().getName(),
					tempChannel.getEmptyMinutes());
			//warn user before channel gets deleted
			if (tempChannel.getEmptyMinutes() > tempChannel.getTimeoutInMinutes()) {
				Logger.info("Channel \"{}\" exceeded it's timeout of {} minutes, deleting channel now",
						tempChannel.getChannel().getName(), tempChannel.getTimeoutInMinutes());
				tempChannelToDelete.add(tempChannel);
				sendPrivateMessage(tempChannel.getOwner(), "Your TempChannel `" + tempChannel.getChannel().getName()
						+ "` got deleted.");
			} else if (tempChannel.getTimeoutInMinutes() < 10 && (tempChannel.getTimeoutInMinutes() - tempChannel.getEmptyMinutes()) == 0){
				sendPrivateMessage(tempChannel.getOwner(), "Your tempChannel `" + tempChannel.getChannel().getName()
						+ "`  will be **deleted in a minute** if you do not use it.");
			} else if ((tempChannel.getTimeoutInMinutes() - tempChannel.getEmptyMinutes()) == 4
					&& tempChannel.getTimeoutInMinutes() >= 10) {
				sendPrivateMessage(tempChannel.getOwner(),"Your tempChannel `" + tempChannel.getChannel().getName()
						+ "` will be **deleted in 5 minutes** if you do not use it.");
			}
		} else if (tempChannel.getEmptyMinutes() > 0) {
			tempChannel.setEmptyMinutes(0);
			Logger.warn("EmptyMinutes > 0 although channel \"{}\" wasn't empty! Set emptyMinutes to 0.", tempChannel.getChannel().getName());
		}
	}
	
	protected void sendPrivateMessage(IUser recepient, String message) {
		if(!recepient.equals(client.getOurUser())){
			try{
				MessageBuilder mb = new MessageBuilder(this.client).withChannel(recepient.getOrCreatePMChannel());
				mb.withContent(message);
				mb.build();
			} catch (DiscordException e){
				Logger.warn("Received DiscordException in sendMessage, user possibly blocked the bot in private chat");
			}
			
		}
		
	}
}
