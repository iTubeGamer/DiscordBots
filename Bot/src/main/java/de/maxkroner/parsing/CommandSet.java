package de.maxkroner.parsing;

import java.util.Set;

public class CommandSet {
	private String commandPrefix;
	private char optionIdentifier;
	private final Set<String> commands;
	
	public CommandSet(String commandPrefix, Set<String> set, char optionIdentifier) {
		this.commandPrefix = commandPrefix;
		this.optionIdentifier = optionIdentifier;
		this.commands = set;
	}

	public String getCommandPrefix() {
		return commandPrefix;
	}
	public char getOptionIdentifier() {
		return optionIdentifier;
	}
	public Set<String> getCommands() {
		return commands;
	}

	public void setCommandPrefix(String commandPrefix) {
		this.commandPrefix = commandPrefix;
	}
	
	
	
}
