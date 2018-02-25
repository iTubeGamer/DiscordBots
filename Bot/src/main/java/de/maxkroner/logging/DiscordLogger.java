package de.maxkroner.logging;

import java.io.IOException;
import java.nio.file.Paths;

import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Logger;
import org.pmw.tinylog.labelers.TimestampLabeler;
import org.pmw.tinylog.policies.DailyPolicy;
import org.pmw.tinylog.writers.RollingFileWriter;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;



public class DiscordLogger{

	public DiscordLogger(String path) {
		super();
		configureLogging(path);
	}
	
	public DiscordLogger(){
		
	}
	
	public void trace(Exception e){
		Logger.trace(e);
	}
	
	public void debug(Exception e){
		Logger.debug(e);
	}
	
	public void info(Exception e){
		Logger.info(e);
	}
	
	public void warn(Exception e){
		Logger.warn(e);
	}
	
	public void error(Exception e){
		Logger.error(e);
	}
	
	public void trace(String message, Object...objects){
		Logger.trace(message, objects);
	}
	
	public void debug(String message, Object...objects){
		Logger.debug(message, objects);
	}
	
	public void info(String message, Object...objects){
		Logger.info(message, objects);
	}
	
	public void warn(String message, Object...objects){
		Logger.warn(message, objects);
	}
	
	public void error(String message, Object...objects){
		Logger.error(message, objects);
	}
	
	public void trace(IGuild guild, IUser user, IChannel channel, String message, Object...objects){
		String advancedMessage = "{} | {} | {} | " + message;
		Logger.trace(advancedMessage + guild.getName(), user.getName(), channel.getName(), objects);
	}
	
	public void debug(IGuild guild, IUser user, IChannel channel, String message, Object... objects){
		String advancedMessage = "{} | {} | {} | " + message;
		Logger.debug(advancedMessage + guild.getName(), user.getName(), channel.getName(), objects);
	}
	
	public void info(IGuild guild, IUser user, IChannel channel, String message, Object... objects){
		String advancedMessage = "{} | {} | {} | " + message;
		Logger.info(advancedMessage + guild.getName(), user.getName(), channel.getName(), objects);
	}
	
	public void warn(IGuild guild, IUser user, IChannel channel, String message, Object... objects){
		String advancedMessage = "{} | {} | {} | " + message;
		Logger.warn(advancedMessage + guild.getName(), user.getName(), channel.getName(), objects);
	}
	
	public void error(IGuild guild, IUser user, IChannel channel, String message, Object... objects){
		String advancedMessage = "{} | {} | {} | " + message;
		Logger.error(advancedMessage + guild.getName(), user.getName(), channel.getName(), objects);
	}
	
	private void configureLogging(String path) {
		try {
			String filename = Paths.get(path).toString() ;
			Configurator.fromResource("tinylog.properties")
						.addWriter(new RollingFileWriter(filename, 1000, new TimestampLabeler("yyyy-MM-dd"), new DailyPolicy()))
						.activate();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
