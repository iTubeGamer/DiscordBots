package de.maxkroner.parsing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Parses Command-objects from messages.
 * There are 2 types of command-styles: argument-style and option-style
 * 		argument-style: !command [argument1 argument2 ...]
 * 		option-style: !command [-option [parameter1 parameter2 ...] ...
 * 
 * @author kroner
 *
 */
public class MessageParsing {
	
	public static Command parseMessageWithCommandSet(String message, CommandSet commandSet){
		if(message.startsWith(commandSet.getCommandIdentifier())){
			String commandString = message.substring(commandSet.getCommandIdentifier().length());
			boolean commandHasOptionsOrArguments = commandString.contains(" ");
			boolean commandIsOptionStyle = commandString.charAt(commandString.indexOf(" ") + 1) == commandSet.getOptionIdentifier();
			String commandName;
			if(commandHasOptionsOrArguments){
				commandName = commandString.substring(0, commandString.indexOf(" "));
			} else {
				commandName = commandString;
				return new Command(commandName, Optional.empty(), Optional.empty());
			}		

			if(commandSet.getCommands().contains(commandName)){
				if(commandIsOptionStyle){
					//parse options
					String[] commandOptionStrings = new String[0];
					List<CommandOption> commandOptions = new ArrayList<>();							
					String optionsString = commandString.substring(commandString.indexOf(" ") + 1);
					//split all options in own Strings
					commandOptionStrings = optionsString.split(String.valueOf(commandSet.getOptionIdentifier()));
					commandOptions = Arrays.stream(commandOptionStrings).map(String::trim).filter(s -> !s.isEmpty())
							.map(s -> parseOptionFromString(s)).collect(Collectors.toList());
					if (commandOptions.isEmpty()){
						commandOptions = null;
					}
					return new Command(commandName, Optional.ofNullable(commandOptions), Optional.empty());
					
				} else {
					//parse arguments
					List<String> arguments = new ArrayList<>();
					String argumentsString = commandString.substring(commandString.indexOf(" ") + 1);
					splitArgumentsIntoList(argumentsString, arguments);
					if(arguments.isEmpty()){
						arguments = null;
					}
					return new Command(commandName, Optional.empty(), Optional.ofNullable(arguments));
				}	
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
	
	private static void splitArgumentsIntoList(String argumentsString, List<String> list){
		StringBuilder builder = new StringBuilder();
		boolean inQuotation = false;
		for (char character : argumentsString.toCharArray()){
			
			if(character != ' ' && character != '"'){
				builder.append(character);
			} else if (character == ' ' && !inQuotation) {
				if(builder.toString().trim().length() > 0){
					list.add(builder.toString().trim());
					builder = new StringBuilder();
				}				
			} else if (character == ' ' && inQuotation) {
				builder.append(character);
			} else if (character == '"'){
				if(inQuotation){
					if(builder.toString().trim().length() > 0){
						list.add(builder.toString().trim());
						builder = new StringBuilder();
					}	
					inQuotation = false;
				} else {
					inQuotation = true;
				}
			}
		}		
		if(builder.toString().trim().length() > 0){
			list.add(builder.toString().trim());
		}	
	}
	
	private static void splitParametersIntoCommandOption(String parameterString, CommandOption commandOption){
		splitArgumentsIntoList(parameterString, commandOption.getParameterList());
	}
}
