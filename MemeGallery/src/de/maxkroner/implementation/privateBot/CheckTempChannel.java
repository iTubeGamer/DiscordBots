package de.maxkroner.implementation.privateBot;

import java.util.ArrayList;
import java.util.concurrent.ScheduledExecutorService;

public class CheckTempChannel implements Runnable {
	private ArrayList<TempChannel> allTempChannel;
	
	public CheckTempChannel(ArrayList<TempChannel> allTempChannel, ScheduledExecutorService executor) {
		super();
		this.allTempChannel = allTempChannel;
	}
	
	@Override
	public void run() {
		if (!allTempChannel.isEmpty()){
			for(TempChannel tempChannel : allTempChannel){
				if(tempChannel.getChannel() != null){
					if (tempChannel.getChannel().getConnectedUsers().isEmpty()){
						tempChannel.setEmptyMinuts(tempChannel.getEmptyMinuts() + 1);
						if(tempChannel.getEmptyMinuts() >= tempChannel.getTimeoutInMinutes()){
							tempChannel.getChannel().delete();
							allTempChannel.remove(tempChannel);
						}
					} else {
						tempChannel.setEmptyMinuts(0);
					}
				} else {
					allTempChannel.remove(tempChannel);
				}
				
			}
		}
	}

}
