package de.maxkroner.implementation.privateBot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import org.pmw.tinylog.Logger;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.MessageBuilder;

public class CheckTempChannel<E> implements Runnable {
	private HashMap<IGuild, TempChannelMap> tempChannelsByGuild;
	IDiscordClient client;

	public CheckTempChannel(HashMap<IGuild, TempChannelMap> channelMap, IDiscordClient client, ScheduledExecutorService executor) {
		super();
		this.client = client;
		this.tempChannelsByGuild = channelMap;
	}

	@Override
	public void run() {
		try {
			// iterate over the tempChannelMaps per Guild to check the timeout
			for (TempChannelMap tempChannelMap : tempChannelsByGuild.values()) {
				// iterate over all the tempChannels of the Guild (collect all TempChannels and delete them later)
				List<TempChannel> tempChannelToDelete = new ArrayList<TempChannel>();
				for (TempChannel tempChannel : tempChannelMap.getAllTempChannel()) {
					// if Channel still exists
					if (!tempChannel.getChannel().isDeleted()) {
						if (tempChannel.getChannel().getConnectedUsers().isEmpty()) {
							tempChannel.setEmptyMinutes(tempChannel.getEmptyMinutes() + 1);
							Logger.info("Channel \"{}\" is empty, increased emptyMinutes to {}", tempChannel.getChannel().getName(),
									tempChannel.getEmptyMinutes());
							//warn user before channel gets deleted
							if (tempChannel.getEmptyMinutes() > tempChannel.getTimeoutInMinutes()) {
								Logger.info("Channel \"{}\" exceeded it's timeout of {} minutes, deleting channel now",
										tempChannel.getChannel().getName(), tempChannel.getTimeoutInMinutes());
								tempChannelToDelete.add(tempChannel);
								sendPrivateMessage(tempChannel.getOwner(), "Your tempChannel \"" + tempChannel.getChannel().getName()
										+ "\" has been empty for reached it's timeout and got deleted.");
							} else if (tempChannel.getTimeoutInMinutes() < 10 && (tempChannel.getTimeoutInMinutes() - tempChannel.getEmptyMinutes()) == 0){
								Logger.debug("telling user his channel will get deleted in a minute");
								sendPrivateMessage(tempChannel.getOwner(), "Your tempChannel \"" + tempChannel.getChannel().getName()
										+ "\" has been empty for " + tempChannel.getEmptyMinutes() + " minutes. It will be deleted in a minute if you do not use it.");
								Logger.debug("ok i told it to him succesfully");
							} else if ((tempChannel.getTimeoutInMinutes() - tempChannel.getEmptyMinutes()) == 4
									&& tempChannel.getTimeoutInMinutes() >= 10) {
								Logger.debug("telling user his channel will get delted in 5 minutes");
								sendPrivateMessage(tempChannel.getOwner(),"Your tempChannel \"" + tempChannel.getChannel().getName()
										+ "\" has been empty for " + tempChannel.getEmptyMinutes() + " minutes. It will be deleted in 5 minutes if you do not use it.");
								Logger.debug("ok i told it to him succesfully");
							}
						} else {
							tempChannel.setEmptyMinutes(0);
							Logger.info("Channel \"{}\" wasn't empty, timeout set to 0", tempChannel.getChannel().getName());
						}
						// if Channel was already deleted
					} else {
						tempChannelToDelete.add(tempChannel);
						Logger.warn("Channel \"{}\" in ChannelMap didn't exist anymore, removed it now!", tempChannel.getChannel().getName());
					}
				}

				for (TempChannel tempChannel : tempChannelToDelete) {
					// delete the VoiceChannel only, the TempChannel will be removed in onVoiceChannelDeleteEvent
					tempChannel.getChannel().delete();
				}
			}
		} catch (Exception e) {
			Logger.error(e);
		}
	}
	
	protected void sendPrivateMessage(IUser recepient, String message) {
		MessageBuilder mb = new MessageBuilder(this.client).withChannel(recepient.getOrCreatePMChannel());
		mb.withContent(message);
		mb.build();
	}
}
