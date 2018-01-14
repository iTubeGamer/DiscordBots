package de.maxkroner.parsing;

import com.google.common.collect.ImmutableSet;

public class CommandSet {
	private String commandIdentifier;
	private char optionIdentifier;
	private final ImmutableSet<String> commands;
	
	public CommandSet(String commandIdentifier, ImmutableSet<String> commands, char optionIdentifier) {
		this.commandIdentifier = commandIdentifier;
		this.optionIdentifier = optionIdentifier;
		this.commands = commands;
	}
	
	/**
	 * Constructor with default commandIdentifier: "!"
	 * @param commands
	 * @param optionIdentifier
	 */
	public CommandSet(ImmutableSet<String> commands, char optionIdentifier) {
		this("!",  commands, optionIdentifier);
	}
	
	/**
	 * Constructor with default optonIdentifier: "-"
	 * @param commandIdentifier
	 * @param commands
	 */
	public CommandSet(String commandIdentifier, ImmutableSet<String> commands) {
		this(commandIdentifier, commands, '-' );
	}
	
	/**
	 * Constructor with default commandIdentifier: "!"
	 * 				and default optonIdentifier: "-"
	 * @param commands
	 */
	public CommandSet(ImmutableSet<String> commands) {
		this("!", commands, '-' );
	}

	public String getCommandIdentifier() {
		return commandIdentifier;
	}
	public char getOptionIdentifier() {
		return optionIdentifier;
	}
	public ImmutableSet<String> getCommands() {
		return commands;
	}
	
	
}
