package de.maxkroner.parsing;

import java.util.List;
import java.util.Optional;

public class Command {
	private String name;
	private Optional<List<CommandOption>> commandOptions;
	private Optional<List<String>> arguments;
	
	public Command(String name, Optional<List<CommandOption>> commandOptions, Optional<List<String>> arguments) {
		super();
		this.name = name;
		this.commandOptions = commandOptions;
		this.arguments = arguments;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Optional<List<CommandOption>> getCommandOptions() {
		return commandOptions;
	}
	public void setCommandOptions(Optional<List<CommandOption>> commandOptions) {
		this.commandOptions = commandOptions;
	}
	public Optional<List<String>> getArguments() {
		return arguments;
	}
	public void setArguments(Optional<List<String>> arguments) {
		this.arguments = arguments;
	}
	
	

}
