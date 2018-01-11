package de.maxkroner.parsing;

public class CommandOption {
	private String commandOptionName;
	private String[] parameterList;
	
	public CommandOption(String commandOptionName){
		this.commandOptionName = commandOptionName;
		this.parameterList = new String[0];
	}
	
	public CommandOption(String commandOptionName, String[] parameterList) {
		super();
		this.commandOptionName = commandOptionName;
		this.parameterList = parameterList;
	}
	public String getCommandOptionName() {
		return commandOptionName;
	}
	public void setCommandOptionName(String commandOptionName) {
		this.commandOptionName = commandOptionName;
	}
	public String[] getParameterList() {
		return parameterList;
	}
	public void setParameterList(String[] parameterList) {
		this.parameterList = parameterList;
	}
	
}
