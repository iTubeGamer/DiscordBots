package de.maxkroner.parsing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MessageParsing {
	
	public static Command parseCommandFromMessage(String message){
		//commands are in the following structure: command [-commandOption parameter ...] ...
		String command_name = message;
		String[] commandOptionStrings = new String[0];
		List<CommandOption> commandOptions = new ArrayList<>();
		
		//if command has a String after the command name
		if (message.contains(" ")) {
			command_name = message.split(" ")[0];
			
			
			String option = message.substring(message.indexOf(" ") + 1);
			//if it is a valid option
			if (option.charAt(0) == '-') {
				//split all options in own Strings
				commandOptionStrings = option.split("-");
				commandOptions = (List<CommandOption>) Arrays.stream(commandOptionStrings).map(String::trim).filter(s -> !s.isEmpty())
						.map(s -> parseOptionFromString(s)).collect(Collectors.toList());
			}
		}	
		
		return new Command(command_name, commandOptions);
	}
	
	private static CommandOption parseOptionFromString(String optionString) {
		CommandOption commandOption;

		if (optionString.contains(" ")) {
			//optionString contains parameters
			commandOption = new CommandOption(optionString.substring(0, optionString.indexOf(" ")));
			String parameterString = optionString.substring(optionString.indexOf(" ") + 1);
			parameterString = parameterString + " ";
			String[] parameterList = Arrays.stream(parameterString.split(" ")).filter(s -> !s.isEmpty()).toArray(String[]::new);
			if (parameterList.length >= 1) {
				commandOption.setParameterList(parameterList);
			}
		} else {
			//optionString does not contain parameters
			commandOption = new CommandOption(optionString);
		}

		return commandOption;
	}
}
