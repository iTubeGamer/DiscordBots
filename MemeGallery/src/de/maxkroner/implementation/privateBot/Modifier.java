package de.maxkroner.implementation.privateBot;

public class Modifier {
	private String modifierName;
	private String[] parameterList;
	
	public Modifier(String modifierName){
		this.modifierName = modifierName;
		this.parameterList = new String[0];
	}
	
	
	public Modifier(String modifierName, String[] parameterList) {
		super();
		this.modifierName = modifierName;
		this.parameterList = parameterList;
	}
	public String getModifierName() {
		return modifierName;
	}
	public void setModifierName(String modifierName) {
		this.modifierName = modifierName;
	}
	public String[] getParameterList() {
		return parameterList;
	}
	public void setParameterList(String[] parameterList) {
		this.parameterList = parameterList;
	}
	
	
}
