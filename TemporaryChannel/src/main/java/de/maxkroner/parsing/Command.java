package de.maxkroner.parsing;

import java.util.List;

public class Command {
	private String name;
	private List<CommandOption> commandOptions;
	
	public Command(String name, List<CommandOption> commandOptions) {
		super();
		this.name = name;
		this.commandOptions = commandOptions;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<CommandOption> getCommandOptions() {
		return commandOptions;
	}
	public void setModifiers(List<CommandOption> commandOptions) {
		this.commandOptions = commandOptions;
	}

}
