package de.maxkroner.implementation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;

import org.discordbots.api.client.DiscordBotListAPI;

import de.maxkroner.database.BotDatabase;
import de.maxkroner.logging.DiscordLogger;
import de.maxkroner.logging.EmptyDiscordLogger;
import de.maxkroner.parsing.Command;
import de.maxkroner.parsing.CommandHandler;
import de.maxkroner.parsing.MessageParsing;
import de.maxkroner.ui.ConsoleMenue;
import de.maxkroner.ui.IConsoleMenue;
import de.maxkroner.values.Values;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.shard.DisconnectedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.Image;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.RequestBuffer;

public abstract class Bot {
	protected String botName;
	protected String homePath;
	protected IConsoleMenue consoleMenue;
	private IDiscordClient client;
	protected BotDatabase db;
	protected static DiscordLogger logger;
	private HashMap<String, HashSet<Method>> commandMethodsMap;
	private HashMap<String, String> aliasCommandMap;
	private String commandPrefix;
	private Character optionIdentifier;
	private Instant startup;
	private Instant lastUpdate;
	protected DiscordBotListAPI api;
	
	/**
	 * 
	 * @param botName name used for home folder
	 */
	public Bot(String botName) {
		 this.botName = botName;
		 setHomePath();
		 startup = Instant.now();
		 lastUpdate = Instant.now();
	}
	
	public Bot addConsoleMenue(IConsoleMenue menue){
		this.consoleMenue = menue;
		return this;
	}
	
	public Bot addConsoleMenue(){
		this.consoleMenue = new ConsoleMenue(this);
		return this;
	}
	
	public Bot addLogging(String loggingPrefix){
		logger = new DiscordLogger(Paths.get(homePath, "log", loggingPrefix + ".log").toString());
		return this;
	}
	
	public Bot addDatabase(String databaseName){
		this.db = new BotDatabase(Paths.get(homePath, "db", databaseName).toString());
		return this;
	}
	
	public Bot addDatabase(BotDatabase database){
		this.db = database;
		return this;
	}
	
	public void run(String token){
		if(logger == null){
			logger = new EmptyDiscordLogger();
		}
		this.client = createClient(token);
		EventDispatcher dispatcher = client.getDispatcher();
		dispatcher.registerListener(this);
	}
	
	protected IDiscordClient getClient() {
		return this.client;
	}

	@EventSubscriber
	public void onReady(ReadyEvent event) {
		Bot bot = this;
		String bot_name = client.getOurUser().getName();
		
		//start consoleMenue if enabled
		if(consoleMenue != null){
			new Thread() {
				@Override
				public void run() {
					consoleMenue.startMenue(bot);
				}
			}.start();
		}
		
		System.out.println("Logged in as " + bot_name);
		logger.info("Logged in as " + bot_name);
	}
	
	@EventSubscriber
	protected void logout(DisconnectedEvent event) {
		System.out.println("Logged out for reason " + event.getReason() + "!");
		logger.info("Logged out for reason " + event.getReason() + "!");
	}
	
	@EventSubscriber
	public void onMessageReceivedEvent(MessageReceivedEvent event) {
		try{
		if(commandMethodsMap != null && !commandMethodsMap.keySet().isEmpty()){
			Command command = MessageParsing.parseMessageWithCommandSet(event.getMessage().getContent(), commandMethodsMap.keySet(), getCommandPrefixForGuild(event.getGuild()), optionIdentifier);
			if(command != null){
				if(commandIsEnabledOnGuild(aliasCommandMap.get(command.getName()), event.getGuild())){
					notifyReceivers(event, command);
				} else {
					sendMessage("The command `" + aliasCommandMap.get(command.getName()) + "` is disabled on the server `" + event.getGuild().getName() + "`", event.getAuthor().getOrCreatePMChannel(), false);
				}
				
			}
		}
		} catch(Exception e){
			logger.error(e);
		}
	}

	@CommandHandler("uptime")
	protected void uptime(MessageReceivedEvent event, Command command){
		if(!command.hasOptionsOrArguments()){
			Duration uptime = Duration.between(startup, Instant.now());
			long hours = uptime.toHours();
			long minutes = uptime.toMinutes() - (hours * 60);
			sendMessage(String.format("Bot uptime: %d hours and %d minutes", hours, minutes), event.getChannel(), false);
		}
	}
	
	@CommandHandler({"prefix"})
	protected void prefix(MessageReceivedEvent event, Command command){
		if(command.hasArguments() && !command.hasOptions() && command.getArguments().get().size() == 1){
			if(!event.getGuild().getOwner().equals(event.getAuthor())){
				sendMessage("This command can only be used by the server owner.", event.getChannel(), false);
				return;
			}
			String prefix = command.getArguments().get().get(0).trim();
			if(prefix.length() >= 1 && prefix.length() <= 10){
				try {
					db.addGuildProperty(event.getGuild().getLongID(), "prefix", prefix);
				} catch (SQLException e) {
					logger.error(e);
				}
				sendMessage("Command prefix has been changed to `" + prefix + "`", event.getChannel(), false);
			} else {
				sendMessage("Please choose a prefix with 1 to 10 characters.", event.getChannel(), false);
			}
		}
	}
	
	@CommandHandler({"enable", "e", "activate"})
	protected void enable(MessageReceivedEvent event, Command command){
		if(command.hasArguments() && !command.hasOptions() && command.getArguments().get().size() == 1){
			String commandName = command.getArguments().get().get(0).trim();
			
			if(commandEnOrDisablingIsOk(event, commandName)){
				try {
					db.addGuildProperty(event.getGuild().getLongID(), aliasCommandMap.get(commandName) + "Enabled", true);
					sendMessage("The command `" + aliasCommandMap.get(commandName) + "` has been enabled", event.getChannel(), false);
				} catch (SQLException e) {
					logger.error(e);
				}
			}
		}
	}
	
	@CommandHandler({"disable", "d", "deactivate"})
	protected void disable(MessageReceivedEvent event, Command command){
		if(command.hasArguments() && !command.hasOptions() && command.getArguments().get().size() == 1){
			String commandName = command.getArguments().get().get(0).trim();
			
			if(commandEnOrDisablingIsOk(event, commandName)){
				try {
					db.addGuildProperty(event.getGuild().getLongID(), aliasCommandMap.get(commandName) + "Enabled", false);
					sendMessage("The command `" + aliasCommandMap.get(commandName) + "` has been disabled", event.getChannel(), false);
				} catch (SQLException e) {
					logger.error(e);
				}
			}
		}
	}

	public void changeName(String name){
		try {
			client.changeUsername(name);
			System.out.println("The botname was changed to \"" + name + "\"");
			logger.info("The botname was changed to \"" + name + "\"");
		} catch (Exception e) {
			System.out.println("Changing botname failed.");
			logger.error(e);
		}
	}
	
	public void changePlayingText(String playingText){
		try {
			client.changePlayingText(playingText);
			System.out.println("The playingText was changed to \"" + playingText + "\"");
			logger.info("The playingText was changed to \"" + playingText + "\"");
		} catch (Exception e) {
			System.out.println("Changing playingText failed.");
			logger.error(e);
		}
	}
	
	public void changeAvatar(String url, String imageType){
		try {
			client.changeAvatar(Image.forUrl(imageType, url));
			System.out.println("The avatar has been successfully changed.");
			logger.info("The avatar has been successfully changed.");
		} catch (Exception e) {
			System.out.println("Chaning avatar failed.");
			logger.error(e);
		}
	}
	
	public abstract void disconnect();
	
	protected void sendMessage(String message, IChannel channel, Boolean tts) {
		RequestBuffer.request(() -> {
			try{
				MessageBuilder mb = new MessageBuilder(this.client).withChannel(channel);
				if (tts)
					mb.withTTS();
				mb.withContent(message);
				mb.build();
			} catch (DiscordException e){
				logger.warn(e);
				throw e;
			}
		});
		
	}
	
	protected void sendPrivateMessage(IUser recepient, String message) {
		MessageBuilder mb = new MessageBuilder(this.client).withChannel(recepient.getOrCreatePMChannel());
		mb.withContent(message);
		mb.build();
	}

	public static IDiscordClient createClient(String token) {
		ClientBuilder clientBuilder = new ClientBuilder();
		clientBuilder.withToken(token);
		try {
			return clientBuilder.login();
											
		} catch (DiscordException e) {
			logger.error(e);
			return null;
		}
	}
	
	//---Message Parsing Event System---//
	protected void addCommandParsing(Class<? extends Bot> botClass, String commandIdentifier, char optionIdentifier, boolean enableStandardCommands){
		if (commandMethodsMap == null){
			commandMethodsMap = new HashMap<>();
		}
		if(aliasCommandMap == null){
			aliasCommandMap = new HashMap<>();
		}
		addCommandsToMethodsMapForClass(botClass);
		if(enableStandardCommands && db != null){
			addCommandsToMethodsMapForClass(Bot.class);
		}
		this.commandPrefix = commandIdentifier;
		this.optionIdentifier = optionIdentifier;
	}
	
	protected void addCommandParsing(Class<? extends Bot> botClass){
		addCommandParsing(botClass, "!", '-', true);
	}

	private void addCommandsToMethodsMapForClass(Class<? extends Bot> botClass) {
		Method[] methods = botClass.getDeclaredMethods();
		for(final Method method: methods){
			if(method.isAnnotationPresent(CommandHandler.class)){
				CommandHandler annotation = method.getAnnotation(CommandHandler.class);
				String[] commands = annotation.value();
				if(commands.length >= 1){
					for(String command : commands){
						addMethodForCommandToMap(command, method);
						//the first command is the command name, the others are aliases
						aliasCommandMap.put(command, commands[0]);
					}
				}
			}
		}
	}

	private void addMethodForCommandToMap(String command, Method method) {
		if(!commandMethodsMap.containsKey(command)){
			commandMethodsMap.put(command, new HashSet<Method>());
		}		
		commandMethodsMap.get(command).add(method);	
	}
	
	private void notifyReceivers(MessageReceivedEvent event, Command command) {
		if (commandMethodsMap.containsKey(command.getName())){
			for(Method method: commandMethodsMap.get(command.getName())){
				try {
					method.invoke(this, event, command);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		}	
	}
	
	private String getCommandPrefixForGuild(IGuild guild) {
		if(db == null){
			return "!";
		}
		long guild_id = guild.getLongID();
		try {
			return db.getStringGuildProperty(guild_id, "prefix").orElse(this.commandPrefix);	
		} catch (SQLException e) {
			logger.error(e);
			return commandPrefix;
		}
	}
	
	private boolean commandIsEnabledOnGuild(String command, IGuild guild) {
		if(db == null){
			return true;
		}
		try {
			return db.getBooleanGuildProperty(guild.getLongID(), command + "Enabled").orElse(true);
		} catch (SQLException e) {
			logger.error(e);
			return false;
		}
	}
	
	private boolean commandEnOrDisablingIsOk(MessageReceivedEvent event, String commandName) {
		if(!event.getGuild().getOwner().equals(event.getAuthor())){
			sendMessage("This command can only be used by the server owner.", event.getChannel(), false);
			return false;
		}
		
		if(Values.commandsThatCantBeDisabled.contains(commandName)){
			sendMessage("This command cannot be activated or deactivated.", event.getChannel(), false);
			return false;
		}
		
		if(!commandMethodsMap.keySet().contains(commandName)){
			sendMessage("The command `" + commandName + "` does not exist.", event.getChannel(), false);
			return false;
		}
		return true;
	}
	
	protected void updateGuildCount(int count, String token, String botId){
		long timeSinceLastUpdate = Duration.between(lastUpdate, Instant.now()).getSeconds();
		if(timeSinceLastUpdate >= 10){
			if(api == null){
				api = new DiscordBotListAPI.Builder()
		                .token(token)
		                .build();
			}
			
			api.setStats(botId, count);
			logger.info("Updated guild count for bot " + botId);
			lastUpdate = Instant.now();
		}
	}
	
	private void setHomePath() {
		String home = System.getProperty("user.home");
		homePath = Paths.get(home, "discordBots", botName).toString() ;	
	}
	

}