package de.maxkroner.implementation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.pmw.tinylog.Logger;

import de.maxkroner.parsing.Command;
import de.maxkroner.parsing.MessageParsing;
import de.maxkroner.implementation.Bot;
import de.maxkroner.model.TempChannel;
import de.maxkroner.model.TempChannelMap;
import de.maxkroner.parsing.CommandOption;
import de.maxkroner.to.TempChannelTO;
import de.maxkroner.ui.TempChannelMenue;
import de.maxkroner.ui.UserInput;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.guild.GuildCreateEvent;
import sx.blah.discord.handle.impl.events.guild.GuildLeaveEvent;
import sx.blah.discord.handle.impl.events.guild.GuildUnavailableEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.guild.voice.VoiceChannelDeleteEvent;
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelJoinEvent;
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelMoveEvent;
import sx.blah.discord.handle.impl.obj.User;
import sx.blah.discord.handle.obj.ICategory;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IDiscordObject;
import sx.blah.discord.handle.obj.IEmoji;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MessageTokenizer;
import sx.blah.discord.util.MessageTokenizer.MentionToken;

public class TempChannelBot extends Bot {
	private static final int timeout_for_unknown_channels = 5; 
	private static ArrayList<String> channelNames = new ArrayList<>();
	private static HashMap<IGuild, TempChannelMap> tempChannelsByGuild = new HashMap<>();
	private static ArrayList<TempChannel> stashedChannels = new ArrayList<>(); // if bot leaves Guild tempChannels get stashed
	private static final EnumSet<Permissions> voice_connect = EnumSet.of(Permissions.VOICE_CONNECT);
	private static final EnumSet<Permissions> empty = EnumSet.noneOf(Permissions.class);
	private static final int USER_CHANNEL_LIMIT = 3;
	private static String path_serialized_tempChannels = "~/discordBots/tempChannels/temp/";
	private static final String file_name = "tempChannels.ser";
	private static String home = "";
	private static boolean still_in_startup_mode = true;

	static {
		fileToArray("channelnames.txt", channelNames, 0);
	}

	public TempChannelBot(String token, Scanner scanner, UserInput userInput) {
		super(token, new TempChannelMenue(scanner, userInput, tempChannelsByGuild));
		home = System.getProperty("user.home");
		path_serialized_tempChannels = Paths.get(home, "discordBots", "tempChannels", "tmp").toString();
	}

	@Override
	public void disconnect() {
		saveTempChannel();
	}

	// ----- EVENT HANDLING ----- //
	@EventSubscriber
	public void onReady(ReadyEvent event) {
		super.onReady(event);
		readChannelNames();

		// create tempChannelMaps
		for (IGuild guild : getClient().getGuilds()) {
			if (!tempChannelsByGuild.containsKey(guild)) {
				TempChannelMap tempChannelMap = new TempChannelMap();
				tempChannelsByGuild.put(guild, tempChannelMap);
			}
		}

		// import previous TempChannels from file
		readTempChannelsFromFile();

		// delete Channel which aren't existent in map
		removeUnkownChannelsForGuild(tempChannelsByGuild.keySet());

		// start Channel-Timout Scheduler
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
		CheckTempChannelRunnable<Runnable> checkEvent = new CheckTempChannelRunnable<Runnable>(tempChannelsByGuild, getClient(), executor);
		executor.scheduleAtFixedRate(checkEvent, 1, 1, TimeUnit.MINUTES);
		Logger.info("TempChannels startet up and ready 2 go!");
		still_in_startup_mode = false;
	}

	@EventSubscriber
	public void onGuildCreateEvent(GuildCreateEvent event) {
		// if the bot is added to a new guild, add guild to map
		IGuild guild = event.getGuild();
		Logger.info("Received GuildCreateEvent for guild {}", guild.getName());
		if (tempChannelsByGuild != null && !still_in_startup_mode) {
			if (!tempChannelsByGuild.containsKey(guild)) {
				TempChannelMap tempChannelMap = new TempChannelMap();
				tempChannelsByGuild.put(guild, tempChannelMap);
				importStashedChannelsForGuild(guild);
				removeUnknownChannelsForGuild(guild);
			}
		}
	}

	@EventSubscriber
	public void onGuildLeaveEvent(GuildLeaveEvent event) {
		IGuild guild = event.getGuild();
		Logger.warn("Received GuildLeaveEvent for guild {}", guild.getName());
		stashChannelsAndRemoveMap(guild);
	}

	@EventSubscriber
	public void onGuildUnavailableEvent(GuildUnavailableEvent event) {
		IGuild guild = event.getGuild();
		Logger.warn("Received GuildUnavailableEvent for guild {}", guild.getName());
		stashChannelsAndRemoveMap(guild);
	}

	@EventSubscriber
	public void onVoiceChannelDelete(VoiceChannelDeleteEvent event) {
		// get Channel that got deleted
		IChannel deletedChannel = event.getVoiceChannel();

		// get TempChannel for the Channel
		TempChannelMap tempChannelMap = tempChannelsByGuild.get(event.getGuild());
		TempChannel tempChannelToRemove = tempChannelMap.getTempChannelForChannel(deletedChannel);

		// delete if TempChannel exists
		if (tempChannelToRemove != null) {
			Logger.info("Removing TempChannel {} from map!", deletedChannel.getName());

			tempChannelMap.removeTempChannel(tempChannelToRemove);
		}
	}

	@EventSubscriber
	public void onMessageReceivedEvent(MessageReceivedEvent event) {
		String message = event.getMessage().getContent();
		Command command = MessageParsing.parseCommandFromMessage(message);

		switch (command.getName()) {
		case "!c":
			executeChannelCommand(command.getCommandOptions(), event);
			break;
		case "!kick":
			executeKickCommand(command.getCommandOptions(), event);
			break;
		case "!ban":
			executeBanCommand(command.getCommandOptions(), event);
			break;
		case "!cc":
			executeChannelClearCommand(command.getCommandOptions(), event);
			break;
		}
	}

	
	  @EventSubscriber public void onUserVoiceChannelJoin(UserVoiceChannelJoinEvent event) { 
		  updateChannelTimeout(event.getVoiceChannel()); 
	  }
	  
	  @EventSubscriber public void onUserVoiceChannelMove(UserVoiceChannelMoveEvent event) {
		  updateChannelTimeout(event.getVoiceChannel()); 	  
	  }
	  
	  private void updateChannelTimeout(IVoiceChannel voiceChannel) { 
		 TempChannelMap tempChannelMap = tempChannelsByGuild.get(voiceChannel.getGuild());
		 if(tempChannelMap != null){
			 TempChannel tempChannel = tempChannelMap.getTempChannelForChannel(voiceChannel);
			 if(tempChannel != null){
				 tempChannel.setEmptyMinutes(0);
				 Logger.info("User joined tempChannel {}, setting empty minutes to 0", voiceChannel.getName());
			 }
		 }
	  }
	 

	// ----- MESSAGE PARSING ----- //
	private void executeChannelCommand(List<CommandOption> commandOptions, MessageReceivedEvent event) {
		Logger.info("Parsing message: {}", event.getMessage().getContent());
		IChannel channel = event.getChannel();
		IUser author = event.getAuthor();
		IGuild guild = event.getGuild();

		// check if User is in voice channel of the guild
		if (author.getVoiceStateForGuild(guild).getChannel() == null) {
			sendMessage(author + " please join a voice channel before activating a channel command.", channel, false);
			return;
		}

		// check if User-Channel-Count-Limit is reached
		if (getUserChannelCountOnGuild(author, guild) >= USER_CHANNEL_LIMIT) {
			sendMessage("Sorry " + author + ", but you reached the personal channel limit of " + USER_CHANNEL_LIMIT
					+ ". Use !cc to delete all of your empty temporary channels. "
					+ "With the option -f you can force to delte all channels, even those who aren't empty!", channel, false);
			Logger.info("User {} reached his channel limit. Channel wasn't created.", author.getName());
			return;
		}

		String name = getRandomName();
		List<IUser> allowedUsers = null; // null = everyone allowed in the new channel
		List<IUser> movePlayers = new ArrayList<IUser>();
		int limit = 0;
		int timeout = 5;
		boolean moveAllowedPlayers = false;

		for (CommandOption option : commandOptions) {

			switch (option.getCommandOptionName()) {
			case "p":
				allowedUsers = parsePrivateOption(option, event);
				break;
			case "m":
				movePlayers = parseMoveOption(commandOptions, option, event, movePlayers);
				if (movePlayers == null) {
					moveAllowedPlayers = true;
				}
				break;
			case "n":
				if (option.getParameterList().length >= 1) {
					name = "";
					for (String parameter : option.getParameterList()) {
						name = name + " " + parameter;
					}

					name = name.substring(1);

				} else {
					sendMessage("Please use the name-option(-n) with a parameter to specify the channel-name. (f.e. !c -n channel_title)", channel,
							false);
				}
				break;
			case "l":
				if (option.getParameterList().length >= 1) {
					int given_limit = Integer.parseInt(option.getParameterList()[0]);
					if (given_limit >= 1 && given_limit <= 99) {
						limit = given_limit;
					} else {
						sendMessage("Please use the user-limit-option (-l) with a parameter to specify the user-limit. (f.e. !c -l 5)", channel,
								false);
					}
				} else {
					sendMessage("Please use the user-limit-option (-l) with a parameter to specify the user-limit. (f.e. !c -l 5)", channel, false);
				}
				break;
			case "t":
				if (option.getParameterList().length >= 1) {
					int given_timeout = Integer.parseInt(option.getParameterList()[0]);
					if (given_timeout >= 1 && given_timeout <= 180) {
						timeout = given_timeout;
					} else {
						sendMessage("Please use the timout-option (-t) only with a parameter between 1-180 minutes (f.e. !c -t 5)", channel, false);
					}
				} else {
					sendMessage("Please use the timout-option (-t) only with a parameter between 1-180 (f.e. !c -t 5)", channel, false);
				}

				break;
			}
		}

		// if all the players that are allowed in the channel (-p) should be moved, add them to the move list
		if (moveAllowedPlayers) {
			movePlayers = new ArrayList<IUser>();
			movePlayers.addAll(allowedUsers);
			movePlayers.add(author);
		}

		createChannel(event, name, allowedUsers, movePlayers, limit, timeout);

	}

	private void executeChannelClearCommand(List<CommandOption> options, MessageReceivedEvent event) {
		Logger.info("Parsing message: {}", event.getMessage().getContent());
		IGuild guild = event.getGuild();
		IUser author = event.getAuthor();
		IChannel channel = event.getChannel();

		// if user has no channels we're done
		if (getUserChannelCountOnGuild(author, guild) == 0) {
			Logger.info("User {} had no tempChannels", event.getAuthor());
			sendMessage(event.getAuthor() + ", you have no temporary channels at the moment!", channel, false);
			return;
		}

		// else, get all user channels
		Collection<TempChannel> userChannels = getUserChannelsOnGuild(author, guild);

		// parse option -f
		boolean forceDelete = false;
		for (CommandOption option : options) {
			switch (option.getCommandOptionName()) {
			case "f":
				forceDelete = true;
				break;
			}
		}

		// delete the channels
		boolean sendMessage = false;
		for (Iterator<TempChannel> iterator = userChannels.iterator(); iterator.hasNext();) {
			IVoiceChannel userChannel = iterator.next().getChannel();
			if (userChannel.getConnectedUsers().isEmpty()) {
				Logger.info("Deleting empty channel {}", userChannel.getName());
				userChannel.delete();
			} else {
				if (forceDelete) {
					Logger.info("Force-Deleting channel {}", userChannel.getName());
					userChannel.delete();
				} else {
					Logger.info("Channel {} isn't empty", userChannel.getName());
					sendMessage = true;
				}
			}
		}

		if (sendMessage) {
			sendMessage("Some of your channels arent empty. Use \"!cc -f\" to force the deletion anyway.", channel, false);
		}

	}

	private void executeKickCommand(List<CommandOption> options, MessageReceivedEvent event) {
		Logger.info("Parsing message: {}", event.getMessage().getContent());
		sendMessage("Kick available soon!", event.getChannel(), false);
	}

	private void executeBanCommand(List<CommandOption> options, MessageReceivedEvent event) {
		Logger.info("Parsing message: {}", event.getMessage().getContent());
		sendMessage("Ban available soon!", event.getChannel(), false);
	}

	private List<IUser> parseMoveOption(List<CommandOption> options, CommandOption option, MessageReceivedEvent event, List<IUser> movePlayers) {

		boolean privateOptionUsed = optionListContainsOptionString("p", options);
		if (option.getParameterList().length > 0 && privateOptionUsed) {
			sendMessage(
					"Tip: If you create a private channel with -p and you don't mention users behind -m, all users you mentioned behind -p will be moved automatically.",
					event.getChannel(), false);
		} else if (option.getParameterList().length == 0 && privateOptionUsed) {
			return null;
		}

		// all users in current channel should be moved
		if (option.getParameterList()[0].equals("all")) {
			if ((event.getAuthor().getVoiceStateForGuild(event.getGuild()).getChannel().getConnectedUsers() != null)) {
				movePlayers = event.getAuthor().getVoiceStateForGuild(event.getGuild()).getChannel().getConnectedUsers();
			}
		} else {
			// users mentioned by name
			parseUserList(option, event, movePlayers, "-m");
		}

		return movePlayers;
	}

	private List<IUser> parsePrivateOption(CommandOption option, MessageReceivedEvent event) {
		// no users mentioned = no private channel
		if (option.getParameterList().length <= 0) {
			sendMessage(
					"You need to specify the users who may join your private channel, when using the private-option -p. (f.e. !c -p @user1 @user2",
					event.getChannel(), false);
			return null;
		}

		List<IUser> allowedUsers = new ArrayList<>();

		// all users in current channel allowed
		if (option.getParameterList()[0].equals("all")) {
			allowedUsers = event.getAuthor().getVoiceStateForGuild(event.getGuild()).getChannel().getConnectedUsers();
			if (allowedUsers.isEmpty()) {
				allowedUsers = null; // none of the mentioned users were
										// recognized -> channel will not be
										// private
			}
			return allowedUsers;
		}

		// users mentioned by name
		parseUserList(option, event, allowedUsers, "-p");
		return allowedUsers;
	}

	private void parseUserList(CommandOption option, MessageReceivedEvent event, List<IUser> userList, String optionString) {
		List<String> notFound = new ArrayList<>();
		for (String parameter : option.getParameterList()) {
			parseUserParameter(event.getGuild(), userList, notFound, parameter);
		}

		if (notFound.size() == option.getParameterList().length) {
			sendMessage("All the user's mentioned behind the option " + optionString + " we're not recognized.", event.getChannel(), false);
			return;
		} else if (!notFound.isEmpty()) {
			String not_found = "";
			for (String username : notFound) {
				not_found = not_found + username + ", ";
			}

			not_found = not_found.substring(0, not_found.length() - 2);

			sendMessage("The following Usernames were not found: " + not_found, event.getChannel(), false);
		}
	}

	private void parseUserParameter(IGuild guild, List<IUser> allowedUsers, List<String> notFound, String parameter) {
		// user mentioned by snowflake-ID
		IUser user;
		MessageTokenizer mt = new MessageTokenizer(getClient(), parameter);
		if (mt.hasNextMention()) {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			MentionToken<IDiscordObject> nextMention = mt.nextMention();
			if (nextMention.getMentionObject().getClass() == User.class) {
				user = (IUser) nextMention.getMentionObject();
				allowedUsers.add(user);
				return;
			}
		}

		// user mentioned by User/-Nickname
		List<IUser> usersByParameter = guild.getUsersByName(parameter, true);
		if (!usersByParameter.isEmpty()) {
			allowedUsers.addAll(usersByParameter);
			return;
		}

		notFound.add(parameter);
	}

	

	// ----- EXCECUTING METHODS ----- //
	private void createChannel(MessageReceivedEvent event, String name, List<IUser> allowedUsers, List<IUser> movePlayers, int limit, int timeout) {
		IGuild guild = event.getGuild();
		IUser owner = event.getAuthor();

		// create the new channel
		IVoiceChannel channel = guild.createVoiceChannel(name);
		Logger.info("Created channel: {}", channel.getName());

		// put channel to temp category
		channel.changeCategory(getTempCategoryForGuild(guild));

		// set user limit
		channel.changeUserLimit(limit);

		// add temp-Channel
		TempChannel tempChannel = new TempChannel(channel, owner, timeout);
		tempChannelsByGuild.get(guild).addTempChannel(tempChannel);

		// set channel permissions
		setChannelPermissions(owner, allowedUsers, guild, channel);

		// move players into new channel
		movePlayersToChannel(movePlayers, channel, event.getAuthor());

		MessageBuilder mb = new MessageBuilder(getClient()).withChannel(event.getChannel());
		mb.withContent("Yo " + owner + ", I created the channel `" + name + "` 4 u m8 ");
		mb.appendContent(getRandomChannelEmoji(guild));
		mb.build();

	}

	private void movePlayersToChannel(List<IUser> playersToMove, IVoiceChannel channel, IUser author) {
		for (IUser user : playersToMove) {
			// only move players who are in the same voice channel as the author
			if (user.getVoiceStateForGuild(channel.getGuild()).getChannel() == author.getVoiceStateForGuild(channel.getGuild()).getChannel()) {
				user.moveToVoiceChannel(channel);
			} else {
				sendPrivateMessage(author, "The user " + user + " wasn't in the same channel as you and therefore couldn't be moved.");
			}

		}
		if (!playersToMove.isEmpty()) {
			Logger.info("Moved players: {}", playersToMove.stream().map(n -> n.getName()).collect(Collectors.joining(", ")));
		}
		;
	}

	private void setChannelPermissions(IUser owner, List<IUser> allowedUsers, IGuild guild, IVoiceChannel channel) {
		// channel owner can do everything
		channel.overrideUserPermissions(owner, EnumSet.allOf(Permissions.class), empty);
		if (allowedUsers != null) {
			// allowedUsers may connect
			for (IUser user : allowedUsers) {
				channel.overrideUserPermissions(user, voice_connect, empty);
			}
			// everyone else may not connect
			channel.overrideRolePermissions(guild.getEveryoneRole(), empty, voice_connect);
		}
	}

	private ICategory getTempCategoryForGuild(IGuild guild) {
		ICategory targetCategory = null;

		List<ICategory> temp_categories = guild.getCategoriesByName("Temporary Channel");
		if (!temp_categories.isEmpty()) {
			targetCategory = temp_categories.get(0);
		} else {
			targetCategory = guild.createCategory("Temporary Channel");
		}

		return targetCategory;
	}

	// ----- HELPER METHODS ----- //
	private void readChannelNames() {
		String line = "";
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		InputStream is = classloader.getResourceAsStream("channelnames.txt");

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			if (is != null) {
				while ((line = reader.readLine()) != null) {
					channelNames.add(line);
				}
			}
		} catch (IOException e) {
			Logger.error(e);
		}

	}

	private static void fileToArray(String fileName, List<String> list, int maxLength) {
		try (InputStream is = TempChannelBot.class.getClassLoader().getResourceAsStream(fileName)) {
			list.addAll(new BufferedReader(new InputStreamReader(is, StandardCharsets.ISO_8859_1)).lines()
					.filter(s -> s != null && s.length() > 0 && (maxLength == 0 || s.length() <= maxLength)).collect(Collectors.toList()));
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}

	private Collection<TempChannel> getUserChannelsOnGuild(IUser user, IGuild guild) {
		return tempChannelsByGuild.get(guild).getTempChannelListForUser(user);
	}

	private int getUserChannelCountOnGuild(IUser user, IGuild guild) {
		return tempChannelsByGuild.get(guild).getUserChannelCount(user);
	}

	private String getRandomName() {
		int index = ThreadLocalRandom.current().nextInt(0, channelNames.size());

		return channelNames.get(index);
	}

	private boolean optionListContainsOptionString(String optionString, List<CommandOption> options) {
		for (CommandOption option : options) {
			if (option.getCommandOptionName().equals(optionString)) {
				return true;
			}
		}

		return false;
	}

	private String getRandomChannelEmoji(IGuild guild) {
		List<IEmoji> channelEmojis = guild.getEmojis();
		if (!channelEmojis.isEmpty()) {
			int index = ThreadLocalRandom.current().nextInt(0, channelEmojis.size());
			return channelEmojis.get(index).toString();
		}

		return "";
	}

	/**
	 * saves all current TempChannels to a file
	 */
	public void saveTempChannel() {
		// create an ArrayList with Transfer Objects (TOs) for each TempChannel
		ArrayList<TempChannelTO> tempChannelTOs = new ArrayList<>();

		// for each tempChannel add a TempChannel TransferObject to the tempChannelTos ArrayList
		tempChannelsByGuild.values().stream()
				.forEach(T -> T.getAllTempChannel().stream().map(TempChannelTO::createFromTempChannel).forEachOrdered(tempChannelTOs::add));

		// save the ArrayList to a file
		writeObjectToFile(tempChannelTOs, path_serialized_tempChannels, file_name);
		Logger.info("{} serialized TempChannels are saved.", tempChannelTOs.size());
	}

	private void writeObjectToFile(Object object, String path, String fileName) {
		try {
			String filePath = Paths.get(path, fileName).toString();
			File file = new File(path);
			if (!file.exists()) {
				file.mkdirs();
			}
			FileOutputStream fileOut = new FileOutputStream(filePath);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(object);
			out.close();
			fileOut.close();
			Logger.info("Serialized objects written to: \"{}\"", filePath);
		} catch (IOException i) {
			Logger.error(i);
		}
	}

	@SuppressWarnings("unchecked")
	private void readTempChannelsFromFile() {
		String pathToFile = Paths.get(path_serialized_tempChannels, file_name).toString();
		try {
			if (new File(pathToFile).exists()) {
				// read ArrayList of TempChannelTOs from file
				ArrayList<TempChannelTO> tempChannelTOs = (ArrayList<TempChannelTO>) readObjectFromFile(pathToFile);
				Logger.info("Read {} serialized TempChannels", tempChannelTOs.size());
				int importedCount = 0;
				// create TempChannels from TOs
				for (TempChannelTO to : tempChannelTOs) {
					IVoiceChannel voiceChannel = getClient().getVoiceChannelByID(to.getChannelSnowflakeID());
					IGuild guild = voiceChannel.getGuild();
					IUser user = guild.getUserByID(to.getOwnerSnowflakeID());
					if ((voiceChannel != null) && !voiceChannel.isDeleted() // if channel still exists
							&& (user != null) // if owner is still in guild
							&& tempChannelsByGuild.containsKey(guild)) { // and bot is still connected to guild
						TempChannel tempChannel = new TempChannel(voiceChannel, user, to.getTimeoutInMinutes(), to.getEmptyMinutes());
						tempChannelsByGuild.get(guild).addTempChannel(tempChannel);
						importedCount++;
					}
				}
				Logger.info("Importet {} from the {} serialized TempChannels", importedCount, tempChannelTOs.size());

			} else {
				Logger.info("No serialized TempChannels found.");
			}

		} catch (Exception e) {
			Logger.error(e);
			return;
		}

	}

	private Object readObjectFromFile(String file) throws FileNotFoundException, IOException, ClassNotFoundException {
		FileInputStream fileIn = new FileInputStream(file);
		ObjectInputStream in = new ObjectInputStream(fileIn);
		Object object = in.readObject();
		in.close();
		fileIn.close();
		return object;
	}
	
	private void removeUnknownChannelsForGuild(IGuild guild){
		boolean textChannelInTempCategory = false;
		ICategory tempCategory = getTempCategoryForGuild(guild);
		if (tempCategory != null) {
			for (IVoiceChannel channel : tempCategory.getVoiceChannels()) {
				if (!tempChannelsByGuild.get(guild).isTempChannelForChannelExistentInMap(channel)) {
					TempChannel tempChannel = new TempChannel(channel, getClient().getOurUser(), timeout_for_unknown_channels);
					tempChannelsByGuild.get(guild).addTempChannel(tempChannel);
					Logger.info("Created 5min timeout TempChannel for unkown channel: {} in guild {}", channel.getName(), guild.getName());
				}
			}
			for (IChannel channel : tempCategory.getChannels()) {
				textChannelInTempCategory = true;
				channel.delete();
				Logger.info("Deleted text channel {} in TempChannel category", channel.getName());
			}
			if (textChannelInTempCategory) {
				sendMessage(
						"Who the hell created a text channel in the TempChannel category? Get your life fixed! I had to clean up this mess for you...",
						guild.getDefaultChannel(), false);
			}
		}
	}

	private void removeUnkownChannelsForGuild(Set<IGuild> guilds) {
		// get TempCategory for each guild and remove VoiceChannels in the category which aren't in the TempChannelMap
		for (IGuild guild : guilds) {
			removeUnknownChannelsForGuild(guild);
		}

	}

	/**
	 * saves TempChannels in stash to reuse them incase the guild gets available again
	 * 
	 * @param guild
	 *            guild for which the tempChannels should be stashed
	 */
	private void stashChannelsAndRemoveMap(IGuild guild) {
		Logger.info("Stashing {} tempChannels from guild {}.", tempChannelsByGuild.get(guild).getAllTempChannel().size(), guild.getName());
		stashedChannels.addAll(tempChannelsByGuild.get(guild).getAllTempChannel());
		tempChannelsByGuild.remove(guild);
	}

	/**
	 * imports the stashed tempChannels from the specified guild
	 * 
	 * @param guild
	 *            the guild for which the stashed tempChannels should be imported
	 */
	private void importStashedChannelsForGuild(IGuild guild) {
		List <IUser> users = guild.getUsers();
		List <IVoiceChannel> channels = guild.getVoiceChannels();
		Iterator<TempChannel> iterator = stashedChannels.parallelStream() //only import TempChannels
				.filter(T -> T.getChannel().getGuild().equals(guild)) //if the guild is correct
				.filter(T -> channels.contains(T.getChannel())) //if the channel still exists
				.filter(T -> users.contains(T.getOwner())) //if the owner is still connected to the server
				.iterator();
		int importedCount = 0;
		while (iterator.hasNext()) {
			tempChannelsByGuild.get(guild).addTempChannel(iterator.next());
			importedCount++;
		}
		
		Logger.info("Imported {} stashed channels for guild {}", importedCount, guild.getName());
	}
	
	

}
