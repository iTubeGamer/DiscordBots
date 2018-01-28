package de.maxkroner.values;

public class Strings {

	// ---COMMANDS---///
	public static final String PREFIX = "gm.";

	public static final String COMMAND_START = "start";
	public static final String COMMAND_START_USAGE = PREFIX + COMMAND_START;

	public static final String COMMAND_JOIN = "join";
	public static final String COMMAND_JOIN_USAGE = PREFIX + COMMAND_JOIN;

	public static final String COMMAND_READY = "ready";
	public static final String COMMAND_READY_USAGE = PREFIX + COMMAND_READY;

	/// ---MESSAGES---///
	public static final String START_GAME_SUCCESS = "Starting game []. Players may join with " + COMMAND_JOIN_USAGE + 
			". Use " + COMMAND_READY_USAGE + "when all players joined.";
	
	public static final String START_GAME_ALREADY_RUNNING = "Cannot start a game in channel [], there is already a game running.";
	
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
