package de.maxkroner.model;

import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;

public class TempChannel {
	private IVoiceChannel channel;
	private IUser owner;
	private int timeoutInMinutes;
	private int emptyMinuts;
	private boolean kickOrBanChannel = false;
	
	public TempChannel(IVoiceChannel channel, IUser owner, int timeoutInMinutes, boolean kickOrBanChannel) {
		super();
		this.channel = channel;
		this.timeoutInMinutes = timeoutInMinutes;
		this.emptyMinuts = 0;
		this.owner = owner;
		this.kickOrBanChannel = kickOrBanChannel;
	}
	
	public TempChannel(IVoiceChannel channel, IUser owner, int timeoutInMinutes, int emptyMinutes) {
		this(channel, owner, timeoutInMinutes, false);
		this.emptyMinuts = emptyMinutes;
	}

	public int getEmptyMinutes() {
		return emptyMinuts;
	}

	public IUser getOwner() {
		return owner;
	}

	public void setEmptyMinutes(int emptyMinutes) {
		this.emptyMinuts = emptyMinutes;
	}

	public int getTimeoutInMinutes() {
		return timeoutInMinutes;
	}

	public IVoiceChannel getChannel() {
		return this.channel;
	}

	public boolean isKickOrBanChannel() {
		return kickOrBanChannel;
	}
	
}
