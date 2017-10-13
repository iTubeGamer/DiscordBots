package de.maxkroner.implementation.privateBot;

import sx.blah.discord.handle.obj.IVoiceChannel;

public class TempChannel {
	private IVoiceChannel channel;
	private int timeoutInMinutes;
	private int emptyMinuts;
	
	public TempChannel(IVoiceChannel channel, int timeoutInMinutes) {
		super();
		this.channel = channel;
		this.timeoutInMinutes = timeoutInMinutes;
		this.emptyMinuts = 0;
	}

	public int getEmptyMinuts() {
		return emptyMinuts;
	}

	public void setEmptyMinuts(int emptyMinuts) {
		this.emptyMinuts = emptyMinuts;
	}

	public int getTimeoutInMinutes() {
		return timeoutInMinutes;
	}

	public IVoiceChannel getChannel() {
		return this.channel;
	}
	
}
