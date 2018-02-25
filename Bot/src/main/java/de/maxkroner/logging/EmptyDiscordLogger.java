package de.maxkroner.logging;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;



public class EmptyDiscordLogger extends DiscordLogger{

	@Override
	public void trace(Exception e){
	}
	
	@Override
	public void debug(Exception e){
	}
	
	@Override
	public void info(Exception e){
	}
	
	@Override
	public void warn(Exception e){
	}
	
	@Override
	public void error(Exception e){
	}
	
	@Override
	public void trace(String message, Object...objects){
	}
	
	@Override
	public void debug(String message, Object...objects){
	}
	
	@Override
	public void info(String message, Object...objects){
	}
	
	@Override
	public void warn(String message, Object...objects){
	}
	
	@Override
	public void error(String message, Object...objects){
	}
	
	@Override
	public void trace(IGuild guild, IUser user, IChannel channel, String message, Object...objects){
	}
	
	@Override
	public void debug(IGuild guild, IUser user, IChannel channel, String message, Object... objects){
	}
	
	@Override
	public void info(IGuild guild, IUser user, IChannel channel, String message, Object... objects){
	}
	
	@Override
	public void warn(IGuild guild, IUser user, IChannel channel, String message, Object... objects){
	}
	
	@Override
	public void error(IGuild guild, IUser user, IChannel channel, String message, Object... objects){
	}

}
