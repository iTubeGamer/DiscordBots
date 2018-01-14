package de.maxkroner.parsing;

import java.util.Set;

public class CommandSet {
	private String commandIdentifier;
	private char optionIdentifier;
	private final Set<String> commands;
	
	public CommandSet(String commandIdentifier, Set<String> set, char optionIdentifier) {
		this.commandIdentifier = commandIdentifier;
		this.optionIdentifier = optionIdentifier;
		this.commands = set;
	}

	public String getCommandIdentifier() {
		return commandIdentifier;
	}
	public char getOptionIdentifier() {
		return optionIdentifier;
	}
	public Set<String> getCommands() {
		return commands;
	}
	
	
}
