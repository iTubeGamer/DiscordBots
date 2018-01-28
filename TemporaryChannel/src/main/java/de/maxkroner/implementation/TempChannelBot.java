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
import java.util.Collection;
import java.util.Collections;
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

import de.maxkroner.implementation.runnable.CheckTempChannelRunnable;
import de.maxkroner.model.TempChannel;
import de.maxkroner.model.TempChannelMap;
import de.maxkroner.parsing.Command;
import de.maxkroner.parsing.CommandHandler;
import de.maxkroner.parsing.CommandOption;
import de.maxkroner.parsing.OptionParsing;
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
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelLeaveEvent;
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelMoveEvent;
import sx.blah.discord.handle.obj.ICategory;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IEmoji;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.RequestBuffer;

public class TempChannelBot extends Bot {
	private static final int timeout_for_unknown_channels = 5;
	private static ArrayList<String> channelNames = new ArrayList<>();
	private static HashMap<IGuild, TempChannelMap> tempChannelsByGuild = new HashMap<>();
	private static ArrayList<TempChannel> stashedChannels = new ArrayList<>(); // if bot leaves Guild tempChannels get stashed
	private static final EnumSet<Permissions> voice_connect = EnumSet.of(Permissions.VOICE_CONNECT);
	private static final EnumSet<Permissions> empty = EnumSet.noneOf(Permissions.class);
	private static final int USER_CHANNEL_LIMIT = 3;
	private static String path_serialized_tempChannels = "~/discordBots/TempChannels/temp/";
	private static final String file_name = "tempChannels.ser";
	private static String home = "";
	private static boolean still_in_startup_mode = true;

	static {
		fileToArray("channelnames.txt", channelNames, 0);
	}

	public TempChannelBot(String token, Scanner scanner, UserInput userInput) {
		super(token, new TempChannelMenue(scanner, userInput, tempChannelsByGuild));
		home = System.getProperty("user.home");
		path_serialized_tempChannels = Paths.get(home, "discordBots", "TempChannels", "tmp").toString();
		addCommandParsing(this.getClass());
	}

	@Override
	public void disconnect() {
		saveTempChannel();
	}

	// ----- EVENT HANDLING ----- //
	@Override
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

	@Override
	@EventSubscriber
	public void onMessageReceivedEvent(MessageReceivedEvent event) {

		if (!event.getChannel().isPrivate()) {
			super.onMessageReceivedEvent(event);
		} else {
			sendMessage(
					"Hey, I'd like to chat with you too, because you are very cute :smile: But unfortunately I am only a bot and therefore I am very akward with strangers...",
					event.getChannel(), false);
			sendMessage("But if you want to send me a command please do it in a server text channel, ok?", event.getChannel(), false);
			return;
		}

	}

	@EventSubscriber
	public void onUserVoiceChannelJoin(UserVoiceChannelJoinEvent event) {
		setEmptyMinutesToZero(event.getVoiceChannel());
	}

	@EventSubscriber
	public void onUserVoiceChannelMove(UserVoiceChannelMoveEvent event) {
		setEmptyMinutesToZero(event.getNewChannel());
		deleteIfKickOrBanChannel(event.getOldChannel());
	}
	
	@EventSubscriber
	public void onUserVoiceChannelLeave(UserVoiceChannelLeaveEvent event) {
		deleteIfKickOrBanChannel(event.getVoiceChannel());
	}

	// ----- COMMAND HANDLING ----- //

	@CommandHandler("c")
	protected void executeChannelCommand(MessageReceivedEvent event, Command command) {
		String name = getRandomName();
		List<IUser> allowedUsers = null; // null = everyone allowed in the new channel
		List<IUser> movePlayers = new ArrayList<IUser>(); // players to move in the new channel
		int limit = 0;
		int timeout = 5;
		boolean moveAllowedPlayers = false;

		List<String> errorMessages = new ArrayList<>();

		if (checkIfPrequisitesAreMet(event.getChannel(), event.getAuthor(), event.getGuild(), errorMessages)) {
			Logger.info("Parsing message: {}", event.getMessage().getContent());

			for (CommandOption option : command.getCommandOptions().orElse(Collections.emptyList())) {

				switch (option.getCommandOptionName()) {
				case "p":
					allowedUsers = OptionParsing.parsePrivateOption(option, event, getClient());
					break;
				case "m":
					movePlayers = OptionParsing.parseMoveOption(option, event, getClient());
					if (movePlayers == null) {
						moveAllowedPlayers = true;
					}
					break;
				case "n":
					name = OptionParsing.parseNameOption(event.getChannel(), name, option, errorMessages);
					break;
				case "l":
					limit = OptionParsing.parseLimitOption(event.getChannel(), limit, option, errorMessages);
					break;
				case "t":
					timeout = OptionParsing.parseTimoutOption(event.getChannel(), timeout, option, errorMessages);
					break;
				}
			}

			// if all the players that are allowed in the channel (-p) should be moved, add them to the move list
			if (moveAllowedPlayers) {
				movePlayers = new ArrayList<IUser>();
				movePlayers.addAll(allowedUsers);
				movePlayers.add(event.getAuthor());
			}
		}

		if (errorMessages.isEmpty()) {
			createTempChannel(event.getGuild(), event.getAuthor(), name, allowedUsers, movePlayers, limit, timeout, false);
		} else {
			sendErrorMessages(event.getChannel(), event.getAuthor(), errorMessages, event.getMessage().getContent());
		}
	}

	@CommandHandler("cc")
	protected void executeChannelClearCommand(MessageReceivedEvent event, Command command) {
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
		for (CommandOption option : command.getCommandOptions().orElse(Collections.emptyList())) {
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

	@CommandHandler("kick")
	protected void executeKickCommand(MessageReceivedEvent event, Command command) {
		Logger.info("Parsing message: {}", event.getMessage().getContent());	
		IVoiceChannel channelToKickFrom = event.getAuthor().getVoiceStateForGuild(event.getGuild()).getChannel();
		TempChannel tempChannelToKickFrom = tempChannelsByGuild.get(event.getGuild()).getTempChannelForChannel(channelToKickFrom);
		if(tempChannelToKickFrom != null && event.getAuthor().equals(tempChannelToKickFrom.getOwner())){
			List<IUser> usersToKick = OptionParsing.parseUserList(command.getArguments().orElse(Collections.emptyList()), getClient());
			if(usersToKick.remove(event.getAuthor())){
				sendMessage("You tried to kick yourself from your own TempChannel :scream:", event.getAuthor().getOrCreatePMChannel(), false);
			}
			if(!usersToKick.isEmpty()){
				TempChannel tempChannel = createTempChannel(event.getGuild(), getClient().getOurUser(), "you got kicked", null, Collections.emptyList(), 0, 0, true);
				movePlayersToChannel(usersToKick, tempChannel.getChannel(), event.getAuthor());
			}	
		} else {
			sendMessage("You can only use this command if you are in a TempChannel that you own", event.getAuthor().getOrCreatePMChannel(), false);
		}
	}

	@CommandHandler("ban")
	protected void executeBanCommand(MessageReceivedEvent event, Command command) {
		Logger.info("Parsing message: {}", event.getMessage().getContent());	
		IVoiceChannel channelToBanFrom = event.getAuthor().getVoiceStateForGuild(event.getGuild()).getChannel();
		TempChannel tempChannelToBanFrom = tempChannelsByGuild.get(event.getGuild()).getTempChannelForChannel(channelToBanFrom);
		if(tempChannelToBanFrom != null && event.getAuthor().equals(tempChannelToBanFrom.getOwner())){
			List<IUser> usersToBan = OptionParsing.parseUserList(command.getArguments().orElse(Collections.emptyList()), getClient());
			if(usersToBan.remove(event.getAuthor())){
				sendMessage("You tried to ban yourself from your own TempChannel :scream:", event.getAuthor().getOrCreatePMChannel(), false);
			}
			if(!usersToBan.isEmpty()){
				denyUsersToJoinChannel(usersToBan, event.getAuthor().getVoiceStateForGuild(event.getGuild()).getChannel());	
				TempChannel tempChannel = createTempChannel(event.getGuild(), getClient().getOurUser(), "you got banned", null, Collections.emptyList(), 0, 0, true);
				movePlayersToChannel(usersToBan, tempChannel.getChannel(), event.getAuthor());
			}	
		} else {
			sendMessage("You can only use this command if you are in a TempChannel that you own", event.getAuthor().getOrCreatePMChannel(), false);
		}		
	}


	@CommandHandler("help")
	protected void executeHelpCommand(MessageReceivedEvent event, Command command) {
		StringBuilder strBuilderCreate = new StringBuilder().append("Command structure:\n")
				.append("`!c [-option [argument1] [argument2] ...] [...]`\n\n").append("List of options:\n")
				.append("`-p [@User1 @User2 ...]`\t ***private:*** *only mentioned users may join*\n")
				.append("`-m [@User1 @User2 ...]`\t ***move:*** *move users into new channel*\n")
				.append("`-t x` \t\t\t\t\t\t\t\t\t\t ***timeout:*** *delete channel after being x minutes empty*\n")
				.append("`-l x` \t\t\t\t\t\t\t\t\t\t ***limit:*** *set user limit to x*\n")
				.append("`-n \"channel name\"`\t\t\t   ***name:*** *give your channel a name*\n\n").append("Example:\n")
				.append("`!c -n \"channel name\" -p @User1 -t 20`");

		StringBuilder strBuilderDelete = new StringBuilder().append("`!cc` \t\t\t  ***channel clear:*** *delete only your empty TempChannels*\n")
				.append("`!cc -f`\t\t ***channel clear force:*** *forces the deletion of all your TempChannels*");

		EmbedBuilder builder = new EmbedBuilder();
		builder.withColor(0, 255, 0).appendField("Create new TempChannel", strBuilderCreate.toString(), false)
				.appendField("Delete all your TempChannels", strBuilderDelete.toString(), false)
				.appendField("Kick User from TempChannel", "`!kick @User1 @User2 ...`", false)
				.appendField("Ban User from TempChannel", "`!ban @User1 @User2 ...`", false);

		RequestBuffer.request(() -> event.getAuthor().getOrCreatePMChannel().sendMessage(builder.build()));
	}

	// ----- EXCECUTING METHODS ----- //
	private TempChannel createTempChannel(IGuild guild, IUser owner, String channelName, List<IUser> allowedUsers, List<IUser> movePlayers, int limit,
			int timeout, boolean kickOrBanChannel) {

		// create the new channel
		IVoiceChannel channel = guild.createVoiceChannel(channelName);
		Logger.info("Created channel: {}", channel.getName());

		// put channel to temp category
		channel.changeCategory(getTempCategoryForGuild(guild));

		// set user limit
		channel.changeUserLimit(limit);

		// add temp-Channel
		TempChannel tempChannel = new TempChannel(channel, owner, timeout, kickOrBanChannel);
		tempChannelsByGuild.get(guild).addTempChannel(tempChannel);

		// set channel permissions
		setChannelPermissions(owner, allowedUsers, guild, channel);

		// move players into new channel
		movePlayersToChannel(movePlayers, channel, owner);

		return tempChannel;
	}

	private void movePlayersToChannel(List<IUser> playersToMove, IVoiceChannel channel, IUser author) {
		for (IUser user : playersToMove) {
			// only move players who are in the same voice channel as the author
			IChannel authorChannel = author.getVoiceStateForGuild(channel.getGuild()).getChannel();
			if (user.getVoiceStateForGuild(channel.getGuild()).getChannel() == authorChannel) {
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
	
	private void denyUsersToJoinChannel(List<IUser> usersToBan, IVoiceChannel channel) {
		for (IUser user : usersToBan) {
			channel.overrideUserPermissions(user, empty, voice_connect);
		}	
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
		// TODO find out how to log after shutdown-hook was called
		// Logger.info("{} serialized TempChannels are saved.", tempChannelTOs.size());
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
			// TODO fix
			// Logger.info("Serialized objects written to: \"{}\"", filePath);
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
					if ((voiceChannel != null) && !voiceChannel.isDeleted()) {// if channel still exists
						IGuild guild = voiceChannel.getGuild();
						IUser user = guild.getUserByID(to.getOwnerSnowflakeID());
						if ((user != null) // if owner is still in guild
								&& tempChannelsByGuild.containsKey(guild)) { // and bot is still connected to guild
							TempChannel tempChannel = new TempChannel(voiceChannel, user, to.getTimeoutInMinutes(), to.getEmptyMinutes());
							tempChannelsByGuild.get(guild).addTempChannel(tempChannel);
							importedCount++;
						}
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

	private void removeUnknownChannelsForGuild(IGuild guild) {
		boolean textChannelInTempCategory = false;
		ICategory tempCategory = getTempCategoryForGuild(guild);
		if (tempCategory != null) {
			for (IVoiceChannel channel : tempCategory.getVoiceChannels()) {
				if (!tempChannelsByGuild.get(guild).isTempChannelForChannelExistentInMap(channel)) {
					TempChannel tempChannel = new TempChannel(channel, getClient().getOurUser(), timeout_for_unknown_channels, false);
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
		List<IUser> users = guild.getUsers();
		List<IVoiceChannel> channels = guild.getVoiceChannels();
		Iterator<TempChannel> iterator = stashedChannels.parallelStream() // only import TempChannels
				.filter(T -> T.getChannel().getGuild().equals(guild)) // if the guild is correct
				.filter(T -> channels.contains(T.getChannel())) // if the channel still exists
				.filter(T -> users.contains(T.getOwner())) // if the owner is still connected to the server
				.iterator();
		int importedCount = 0;
		while (iterator.hasNext()) {
			tempChannelsByGuild.get(guild).addTempChannel(iterator.next());
			importedCount++;
		}

		Logger.info("Imported {} stashed channels for guild {}", importedCount, guild.getName());
	}

	private void setEmptyMinutesToZero(IVoiceChannel voiceChannel) {
		TempChannelMap tempChannelMap = tempChannelsByGuild.get(voiceChannel.getGuild());
		if (tempChannelMap != null) {
			TempChannel tempChannel = tempChannelMap.getTempChannelForChannel(voiceChannel);
			if (tempChannel != null) {
				tempChannel.setEmptyMinutes(0);
				Logger.info("User joined tempChannel {}, setting empty minutes to 0", voiceChannel.getName());
			}
		}
	}
	
	private void deleteIfKickOrBanChannel(IVoiceChannel voiceChannel) {
		TempChannelMap tempChannelMap = tempChannelsByGuild.get(voiceChannel.getGuild());
		if (tempChannelMap != null) {
			TempChannel tempChannel = tempChannelMap.getTempChannelForChannel(voiceChannel);
			if (tempChannel != null && tempChannel.isKickOrBanChannel() && tempChannel.getChannel().getConnectedUsers().isEmpty()) {
				tempChannel.getChannel().delete();
			}
		}
	}

	private void sendErrorMessages(IChannel channel, IUser author, List<String> errorMessages, String originalMessage) {
		if (errorMessages.size() == 1) {
			sendMessage("Ey " + author + ": " + errorMessages.get(0), channel, false);
		} else {
			sendMessage("Ey " + author + ", there was something wrong with your channel command. Look in private chat for further information.",
					channel, false);
			MessageBuilder mb = new MessageBuilder(getClient()).withChannel(author.getOrCreatePMChannel());
			mb.appendContent("There were some things wrong with your channel command: \"" + originalMessage + "\"\n");
			int count = 1;
			for (String errMessage : errorMessages) {
				mb.appendContent("\t" + count + ". " + errMessage);
				mb.appendContent("\n");
				count++;
			}
			mb.build();
		}
	}

	private boolean checkIfPrequisitesAreMet(IChannel channel, IUser author, IGuild guild, List<String> errorMessages) {
		// check if User is in voice channel of the guild
		if (author.getVoiceStateForGuild(guild).getChannel() == null) {
			errorMessages.add("Please join a voice channel before activating a channel command.");
			Logger.info("Received channel command, but user wasn't in a voiceChannel");
			return false;
		}

		// check if User-Channel-Count-Limit is reached
		if (getUserChannelCountOnGuild(author, guild) >= USER_CHANNEL_LIMIT) {
			errorMessages.add(
					"You reached the personal channel limit of " + USER_CHANNEL_LIMIT + ". Use !cc to delete all of your empty temporary channels. "
							+ "With the option -f you can force to delete all channels, even those who aren't empty!");
			Logger.info("Received channel command, but user has already reached his channel limit");
			return false;
		}

		return true;
	}

}
