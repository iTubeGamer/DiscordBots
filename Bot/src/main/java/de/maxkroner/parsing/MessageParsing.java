package de.maxkroner.parsing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.obj.User;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IDiscordObject;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.MessageTokenizer;
import sx.blah.discord.util.MessageTokenizer.MentionToken;

public class MessageParsing {
	
	public static Command parseMessageWithCommandSet(String message, CommandSet commandSet){
		if(message.startsWith(commandSet.getCommandIdentifier())){
			String commandString = message.substring(commandSet.getCommandIdentifier().length());
			boolean commandHasOptions = commandString.contains(" ");
			String commandName;
			if(commandHasOptions){
				commandName = commandString.substring(0, commandString.indexOf(" "));
			} else {
				commandName = commandString;
			}		

			if(commandSet.getCommands().contains(commandName)){
				String[] commandOptionStrings = new String[0];
				List<CommandOption> commandOptions = new ArrayList<>();				
				if (commandHasOptions) {				
					String option = commandString.substring(commandString.indexOf(" ") + 1);
					//if it is a valid option
					if (option.charAt(0) == commandSet.getOptionIdentifier()) {
						//split all options in own Strings
						commandOptionStrings = option.split(String.valueOf(commandSet.getOptionIdentifier()));
						commandOptions = (List<CommandOption>) Arrays.stream(commandOptionStrings).map(String::trim).filter(s -> !s.isEmpty())
								.map(s -> parseOptionFromString(s)).collect(Collectors.toList());
					}
				}	
				return new Command(commandName, commandOptions);
			}	
		}
		
		return null;
	}
	
	private static CommandOption parseOptionFromString(String optionString) {
		CommandOption commandOption;

		if (optionString.contains(" ")) {
			//optionString contains parameters
			commandOption = new CommandOption(optionString.substring(0, optionString.indexOf(" ")));
			String parameterString = optionString.substring(optionString.indexOf(" ") + 1);
			parameterString = parameterString + " ";
			splitParametersIntoCommandOption(parameterString, commandOption);
		} else {
			//optionString does not contain parameters
			commandOption = new CommandOption(optionString);
		}

		return commandOption;
	}
	
	private static void splitParametersIntoCommandOption(String parameterString, CommandOption commandOption){
		StringBuilder builder = new StringBuilder();
		boolean inQuotation = false;
		for (char character : parameterString.toCharArray()){
			
			if(character != ' ' && character != '"'){
				builder.append(character);
			} else if (character == ' ' && !inQuotation) {
				if(builder.length() > 0){
					commandOption.addParameter(builder.toString());
					builder = new StringBuilder();
				}				
			} else if (character == ' ' && inQuotation) {
				builder.append(character);
			} else if (character == '"'){
				if(inQuotation){
					if(builder.length() > 0){
						commandOption.addParameter(builder.toString());
						builder = new StringBuilder();
					}	
					inQuotation = false;
				} else {
					inQuotation = true;
				}
			}
		}		
	}
}
