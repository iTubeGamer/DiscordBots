package de.maxkroner.values;

public class Values {

	// ---COMMANDS---///
	public static final String PREFIX = "gm.";
	public static final Character OPTION_PREFIX = '-';

	public static final String COMMAND_CREATE = "create";
	public static final String COMMAND_CREATE_USAGE = PREFIX + COMMAND_CREATE;
	
	public static final String COMMAND_START = "start";
	public static final String COMMAND_START_USAGE = PREFIX + COMMAND_START;

	public static final String COMMAND_JOIN = "join";
	public static final String COMMAND_JOIN_USAGE = PREFIX + COMMAND_JOIN;
	
	public static final String COMMAND_SCOREBOARD = "score";
	public static final String COMMAND_STOP_SCOREBOARD = PREFIX + COMMAND_SCOREBOARD;
	
	public static final String COMMAND_STOP = "stop";
	public static final String COMMAND_STOP_USAGE = PREFIX + COMMAND_STOP;
	
	
	
	///---GAMES---///
	public static final String GAME_GUESS_THE_PIC_NAME = "Guess The Pic";
	public static final String GAME_GUESS_THE_PIC_COMMAND = "pic";
	
	///---METHODS---///
	public static final String getMessageString(String message, String... arguments){
		String messageText = message;
		int argNumber = 0;
		while(messageText.contains("{}") && argNumber < arguments.length){
			messageText = messageText.replaceFirst("\\{\\}", arguments[argNumber]);
			argNumber++;
		}		
		return messageText;		
	}
	
	///---MESSAGES---///
	public static final String MESSAGE_CREATE_GAME_SUCCESS = "Creating game \"{}\". Players may join with `" + COMMAND_JOIN_USAGE + 
			"`. Use `" + COMMAND_START_USAGE + "` when all players joined.";
	
	public static final String MESSAGE_CREATE_GAME_ALREADY_EXISTS = "Cannot create a game in channel {}, there is already an existing game.";
	
	public static final String MESSAGE_PLAYER_JOINED = "{} joined the game.";
	
	public static final String MESSAGE_ALREADY_ON_PLAYERLIST = "{} you are already on the player list.";
	
	public static final String MESSAGE_GAME_OVER = "The game \"{}\" is over.";
	
	//GuessThePic
	public static final String MESSAGE_GTP_NEW_ROUND = "Starting Round {}";
	
	public static final String MESSAGE_GTP_QUESTION = "What am I searching for on google images?";
	
	public static final String MESSAGE_GTP_RIGHT_GUESS = "{} guessed it! The word was: {}.";
	
	public static final String MESSAGE_GTP_SKIP_ROUND = "Skipping round {}. The word was: {}.";
	
	public static final String MESSAGE_GTP_HINT_1 = "`Hint: {}`";
	
	public static final String MESSAGE_ERROR_IMAGE_SERVICE = "Image-Service currently not available, game over :(";
	
	public static final String COMMAND_GTP_NEXT_ROUND = "nextround";
	public static final String COMMAND_GTP_NEXT_ROUND_USAGE = PREFIX + COMMAND_GTP_NEXT_ROUND;
	
	public static final int MAX_IMAGES_SHOWN = 3;
}
