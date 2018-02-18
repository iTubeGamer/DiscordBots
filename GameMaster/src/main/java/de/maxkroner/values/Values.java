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
	
	//---GuessThePic---//
	
	public static final String MESSAGE_GTP_NEW_ROUND = "Starting Round {}";
	
	public static final String MESSAGE_GTP_QUESTION = "What am I searching for on google images?";
	
	public static final String MESSAGE_GTP_RIGHT_GUESS = "{} guessed it! The word was: {}.";
	
	public static final String MESSAGE_GTP_SKIP_ROUND = "Skipping round {}. The word was: {}.";
	
	public static final String MESSAGE_GTP_HINT_1 = "`Hint: {}`";
	
	public static final String MESSAGE_GTP_INVALID_LIST = "The list {} does not exist.";
	
	public static final String MESSAGE_ERROR = "An error occured :(";
	
	public static final String MESSAGE_SQL_ERROR_NESTING = "Can not nest two lists into each other!";
	
	public static final String COMMAND_GTP_NEXT_ROUND = "nextround";
	public static final String COMMAND_GTP_NEXT_ROUND_USAGE = PREFIX + COMMAND_GTP_NEXT_ROUND;
	
	public static final int IMAGES_SHOWN_PER_ROUND = 3;
	
	public static final int SHOW_IMAGE_DELAY = 20;
	
	public static final int ROUND_DELAY_IN_SECONDS = 1;
	
	public static final int IMAGES_PER_WORD_IN_DB = 15;
	
	public static final String URL_PATTERN = "^[0-9a-zA-Z\\/\\$\\-_.+!*'(),,;?:@=& ]{5,500}\\.(jpg|Jpg|JPG|jpeg|JPEG|png|Png|PNG)$";
	
	public static final String GTP_SQL_CREATE_IMAGES = "CREATE TABLE IF NOT EXISTS images(" +
				"image_id bigint auto_increment PRIMARY KEY, " +
				"url varchar(255) NOT NULL, " +
				"word_id bigint, " +
				"FOREIGN KEY(word_id) REFERENCES words(word_id)," +
				"CONSTRAINT UC_Image UNIQUE (url,word_id));";
	
	public static final String GTP_SQL_CREATE_WORDS = "CREATE TABLE IF NOT EXISTS words(" +
			"word_id bigint auto_increment PRIMARY KEY," +
			"name varchar(255) NOT NULL," +
			"list_id bigint, " +
			"FOREIGN KEY(list_id) REFERENCES lists(list_id)," +
			"CONSTRAINT UC_Word UNIQUE (name,list_id));";
	
	public static final String GTP_SQL_CREATE_LISTS = "CREATE TABLE IF NOT EXISTS lists(" +
			"list_id bigint auto_increment PRIMARY KEY," +
			"name varchar(255) NOT NULL," +
			"language varchar(255) NOT NULL," +
			"description varchar(255) NOT NULL," +
			"guild_id bigint NOT NULL, " +
			"CONSTRAINT UC_List UNIQUE (name,guild_id));";

	
	public static final String GTP_SQL_CREATE_LIST_NESTING = "CREATE TABLE IF NOT EXISTS listnesting(" +
			"nesting_id bigint auto_increment PRIMARY KEY," +
			"outer_list_id bigint NOT NULL," +
			"inner_list_id bigint NOT NULL," +
			"FOREIGN KEY(outer_list_id, inner_list_id) REFERENCES lists(list_id, list_id)," +
			"UNIQUE (outer_list_id,inner_list_id)," +
			"CHECK(outer_list_id <> inner_list_id));";
	
	public static final String GTP_SQL_NESTING_INTEGRITY_TRIGGER = "CREATE TRIGGER IF NOT EXISTS NESTING_INTEGRITY BEFORE INSERT ON listnesting FOR EACH ROW CALL \"org.h2.trigger.NestingIntegrityTrigger\"";
	
}
