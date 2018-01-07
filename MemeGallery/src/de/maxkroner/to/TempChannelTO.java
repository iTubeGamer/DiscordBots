package de.maxkroner.to;

import java.io.Serializable;

public class TempChannelTO implements Serializable {
	private static final long serialVersionUID = 7849100261328007354L;

	private long channelSnowflakeID;
	private long ownerSnowflakeID;
	private int timeoutInMinutes;
	private int emptyMinutes;

	public TempChannelTO(long channelSnowflakeID, long ownerSnowflakeID, int timeoutInMinutes, int emptyMinutes) {
		super();
		this.channelSnowflakeID = channelSnowflakeID;
		this.ownerSnowflakeID = ownerSnowflakeID;
		this.timeoutInMinutes = timeoutInMinutes;
		this.emptyMinutes = emptyMinutes;
	}

	public long getChannelSnowflakeID() {
		return channelSnowflakeID;
	}

	public void setChannelSnowflakeID(long channelSnowflakeID) {
		this.channelSnowflakeID = channelSnowflakeID;
	}

	public long getOwnerSnowflakeID() {
		return ownerSnowflakeID;
	}

	public void setOwnerSnowflakeID(long ownerSnowflakeID) {
		this.ownerSnowflakeID = ownerSnowflakeID;
	}

	public int getTimeoutInMinutes() {
		return timeoutInMinutes;
	}

	public void setTimeoutInMinutes(int timeoutInMinutes) {
		this.timeoutInMinutes = timeoutInMinutes;
	}

	public int getEmptyMinutes() {
		return emptyMinutes;
	}

	public void setEmptyMinutes(int emptyMinutes) {
		this.emptyMinutes = emptyMinutes;
	}

}
