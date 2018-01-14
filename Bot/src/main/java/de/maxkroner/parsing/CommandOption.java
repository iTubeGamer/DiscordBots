package de.maxkroner.parsing;

import java.util.ArrayList;
import java.util.List;

public class CommandOption {
	private String commandOptionName;
	private List<String> parameterList;
	
	public CommandOption(String commandOptionName){
		this.commandOptionName = commandOptionName;
		this.parameterList = new ArrayList<String>();
	}
	
	public CommandOption(String commandOptionName, List<String> parameterList) {
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
	public List<String> getParameterList() {
		return parameterList;
	}
	public void addParameter(String parameter){
		parameterList.add(parameter);
	}
	
}
