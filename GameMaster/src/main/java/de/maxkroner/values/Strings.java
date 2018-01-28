package de.maxkroner.values;

public class Strings {

	// ---COMMANDS---///
	public static final String PREFIX = "gm.";

	public static final String COMMAND_CREATE = "create";
	public static final String COMMAND_CREATE_USAGE = PREFIX + COMMAND_CREATE;
	
	public static final String COMMAND_START = "start";
	public static final String COMMAND_START_USAGE = PREFIX + COMMAND_START;

	public static final String COMMAND_JOIN = "join";
	public static final String COMMAND_JOIN_USAGE = PREFIX + COMMAND_JOIN;
	
	///---GAMES---///
	public static final String GUESS_THE_PIC_NAME = "Guess The Pic";
	public static final String GUESS_THE_PIC_COMMAND = "gtp";
	

	/// ---MESSAGES---///
	public static final String CREATE_GAME_SUCCESS = "Creating game []. Players may join with " + COMMAND_JOIN_USAGE + 
			". Use " + COMMAND_START_USAGE + "when all players joined.";
	
	public static final String CREATE_GAME_ALREADY_EXISTS = "Cannot create a game in channel [], there is already an existing game.";
	
	///---METHODS---///
	public static final String getMessageString(String message, String... arguments){
		String messageText = message;
		int argNumber = 0;
		while(messageText.contains("[]") && argNumber < arguments.length){
			message.replaceFirst("[]", arguments[argNumber]);
			argNumber++;
		}		
		return messageText;		
	}
}
