package de.maxkroner.implementation.privateBot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import de.maxkroner.implementation.Bot;
import de.maxkroner.ui.PrivateBotMenue;
import de.maxkroner.ui.UserInput;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.guild.channel.ChannelDeleteEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelJoinEvent;
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelMoveEvent;
import sx.blah.discord.handle.obj.ICategory;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.handle.obj.Permissions;

public class PrivateBot extends Bot {
	private static final String token = "MzY3NjY1NzIwMDU1ODI0Mzg0.DL-0Pg.UkGtH2Y8xTCQbWDAmUqGJdykbW8";
	private static ArrayList<String> channelNames = new ArrayList<>();
	private static HashMap<IGuild, TempChannelMap> tempChannelsByGuild = new HashMap<>();
	private static final EnumSet<Permissions> voice_connect = EnumSet.of(Permissions.VOICE_CONNECT);
	private static final EnumSet<Permissions> empty = EnumSet.noneOf(Permissions.class);
	private static final int USER_CHANNEL_LIMIT = 3;

	public PrivateBot(Scanner scanner, UserInput userInput) {
		super(token, new PrivateBotMenue(scanner, userInput));
	}

	// ----- EVENT HANDLING ----- //
	@EventSubscriber
	public void onReady(ReadyEvent event) {
		super.onReady(event);
		readChannelNames();

		// start Channel-Timout
		for (IGuild guild : client.getGuilds()) {
			TempChannelMap tempChannelMap = new TempChannelMap();
			tempChannelsByGuild.put(guild, tempChannelMap);
		}
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
		CheckTempChannel<Runnable> checkEvent = new CheckTempChannel<Runnable>(tempChannelsByGuild, executor);
		executor.scheduleAtFixedRate(checkEvent, 10, 10, TimeUnit.MINUTES);

	}

	@EventSubscriber
	public void onChannelDelete(ChannelDeleteEvent event) {
		// get Channel that got deleted
		IChannel deltedChannel = event.getChannel();
		// get TempChannelMap for the guild
		TempChannelMap tempChannelMap = tempChannelsByGuild.get(event.getGuild());
		// get the TempChannel for the Channel
		TempChannel tempChannelToRemove = tempChannelMap.getTempChannelForChannel(deltedChannel);
		// remove TempChannel from the map
		tempChannelMap.removeTempChannel(tempChannelToRemove);
	}

	@EventSubscriber
	public void onMessageReceivedEvent(MessageReceivedEvent event) {
		String message = event.getMessage().getContent();
		IChannel channel = event.getMessage().getChannel();

		// scan for messages in the following structure: command [-modifier
		// [paramter ...] ...] ...
		String command = message;
		String[] modifierStrings = new String[0];
		List<Modifier> modifierList = new ArrayList<>();
		if (message.contains(" ")) {
			command = message.split(" ")[0];

			String allModifiersString = message.substring(message.indexOf(" ") + 1);
			if (allModifiersString.charAt(0) == '-') {
				modifierStrings = allModifiersString.split("-");
				// modifierStrings =
				// Arrays.stream(modifierStrings).map(String::trim).filter(s ->
				// !s.isEmpty()).toArray(String[]::new);
				modifierList = (List<Modifier>) Arrays.stream(modifierStrings).map(String::trim)
						.filter(s -> !s.isEmpty()).map(s -> parseModifierFromString(s)).collect(Collectors.toList());
			}
		}

		switch (command) {
		case "!channel":
			parseChannelCommand(event, modifierList);
			break;
		case "!kick":
			parseKickCommand(modifierList, channel);
			break;
		case "!channelclear":
			parseChannelClearCommand(event, modifierList);
			break;
		}
	}

	// not used, for better performance
	/**
	 * @EventSubscriber public void
	 *                  onUserVoiceChannelJoin(UserVoiceChannelJoinEvent event)
	 *                  { //updateChannelTimeout(event.getVoiceChannel()); }
	 * 
	 * @EventSubscriber public void
	 *                  onUserVoiceChannelMove(UserVoiceChannelMoveEvent event)
	 *                  { //updateChannelTimeout(event.getVoiceChannel()); }
	 * 
	 *                  private void updateChannelTimeout(IVoiceChannel
	 *                  voiceChannel) { for (TempChannel tempChannel :
	 *                  tempChannels) { if
	 *                  (tempChannel.getChannel().equals(voiceChannel)) {
	 *                  tempChannel.setEmptyMinuts(0); } } }
	 **/

	// ----- MESSAGE PARSING ----- //
	private void parseChannelClearCommand(MessageReceivedEvent event, List<Modifier> modifierList) {
		IGuild guild = event.getGuild();
		IUser author = event.getAuthor();
		IChannel channel = event.getChannel();

		// if user has no channels we're done
		if (getUserChannelCountOnGuild(author, guild) == 0) {
			sendMessage(event.getAuthor() + ", you have no temporary channels at the moment!", channel, false);
			return;
		}

		// else, get all user channels
		Collection<TempChannel> userChannels = getUserChannelsOnGuild(author, guild);

		// parse modifier -f
		boolean forceDelete = false;
		for (Modifier modifier : modifierList) {
			switch (modifier.getModifierName()) {
			case "f":
				forceDelete = true;
				break;
			}
		}

		// delete the channels
		for (Iterator<TempChannel> iterator = userChannels.iterator(); iterator.hasNext();) {
			IVoiceChannel userChannel = iterator.next().getChannel();
			if (forceDelete || userChannel.getConnectedUsers().isEmpty()) {
				userChannel.delete();
			}
		}
	}

	private void parseKickCommand(List<Modifier> modifierList, IChannel channel) {
		sendMessage("Kick available soon!", channel, false);
	}

	private void parseChannelCommand(MessageReceivedEvent event, List<Modifier> modifierList) {
		IChannel channel = event.getChannel();
		IUser author = event.getAuthor();
		IGuild guild = event.getGuild();

		// check if User-Channel-Count-Limit is reached
		if (getUserChannelCountOnGuild(author, guild) >= USER_CHANNEL_LIMIT) {
			sendMessage("Sorry " + author.getName() + ", but you reached the personal channel limit of "
					+ USER_CHANNEL_LIMIT + ". Use !channelclear to delete all of your empty temporary channels. "
					+ "With the modifier -f you can force to delte all channels, even those who aren't empty!", channel,
					false);
			return;
		}

		String name = getRandomName();
		List<IUser> allowedUsers = null; // null = everyone allowed
		List<IUser> movePlayers = new ArrayList<IUser>();
		boolean move = false;
		int limit = 0;
		int timeout = 5;

		for (Modifier modifier : modifierList) {

			switch (modifier.getModifierName()) {
			case "p":
				parsePrivateModifier(modifier, event, allowedUsers);
				break;
			case "m":
				parseMoveModifier(modifierList, movePlayers, modifier, event, movePlayers);
				break;
			case "n":
				if (modifier.getParameterList().length >= 1) {
					name = "";
					for (String parameter : modifier.getParameterList()) {
						name = name + " " + parameter;
					}

					name = name.substring(1);

				} else {
					sendMessage(
							"Please use the name-modifier (-n) with a parameter to specify the channel-name. (f.e. !channel -n channel_title)",
							channel, false);
				}
				break;
			case "t":
				if (modifier.getParameterList().length >= 1) {
					int given_limit = Integer.parseInt(modifier.getParameterList()[0]);
					if (given_limit >= 10 && given_limit <= 180) {
						limit = given_limit;
					} else {
						sendMessage(
								"Please use the timout-modifier (-t) only with a parameter between 10-180 minutes (f.e. !channel -t 5)",
								channel, false);
					}
				} else {
					sendMessage(
							"Please use the timout-modifier (-t) only with a parameter between 1-60 (f.e. !channel -t 5)",
							channel, false);
				}

				break;
			}
		}

		createChannel(event, name, allowedUsers, movePlayers, limit, timeout);

	}

	private void parseMoveModifier(List<Modifier> modifierList, List<IUser> allowedUsers, Modifier modifier,
			MessageReceivedEvent event, List<IUser> movePlayers) {

		boolean privateModifierUsed = modifierListContainsModifierstring("p", modifierList);
		if (modifier.getParameterList().length > 0 && privateModifierUsed) {

			sendMessage(
					"Tip: If you create a private channel with -p and you don't mention users behind -m, all users you mentioned behind -p will be moved automatically.",
					event.getChannel(), false);
			// all users in current channel should be moved
			if (modifier.getParameterList()[0].equals("all")) {
				movePlayers = event.getAuthor().getVoiceStateForGuild(event.getGuild()).getChannel()
						.getConnectedUsers();
				return;
			}

			// users mentioned by name
			parseUserList(modifier, event, movePlayers, "-m");
		} else {
			if (privateModifierUsed) {
				movePlayers = allowedUsers;
			}
		}
	}

	private void parsePrivateModifier(Modifier modifier, MessageReceivedEvent event, List<IUser> allowedUsers) {
		// no users mentioned = no private channel
		if (modifier.getParameterList().length <= 0) {
			sendMessage(
					"You need to specify the users who may join your private channel, when using the private modifier -p. (f.e. !channel -p @user1 @user2",
					event.getChannel(), false);
			return;
		}

		allowedUsers = new ArrayList<IUser>();

		// all users in current channel allowed
		if (modifier.getParameterList()[0].equals("all")) {
			allowedUsers = event.getAuthor().getVoiceStateForGuild(event.getGuild()).getChannel().getConnectedUsers();
			if(allowedUsers.isEmpty()){
				allowedUsers = null; //none of the mentioned users were recognized -> channel will not be private
			}
			return;
		}

		// users mentioned by name
		parseUserList(modifier, event, allowedUsers, "-p");
	}

	private void parseUserList(Modifier modifier, MessageReceivedEvent event, List<IUser> userList,
			String modifierString) {
		List<String> notFound = new ArrayList<>();
		for (String parameter : modifier.getParameterList()) {
			parseUserParameter(event.getGuild(), userList, notFound, parameter);
		}

		if (notFound.size() == modifier.getParameterList().length) {
			sendMessage("All the user's mentioned behind the modifier " + modifierString + " we're not recognized.",
					event.getChannel(), false);
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
		if (parameter.startsWith("<@!")) {
			IUser userByID = guild.getUserByID(Long.parseLong(parameter.substring(3, parameter.length() - 1)));
			if (userByID != null) {
				allowedUsers.add(userByID);
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

	private Modifier parseModifierFromString(String modifierString) {
		Modifier modifier = new Modifier((modifierString + " ").substring(0, (modifierString + " ").indexOf(" ")));

		if (modifierString.contains(" ")) {
			String parameterString = modifierString.substring(modifierString.indexOf(" ") + 1);
			parameterString = parameterString + " ";
			String[] parameterList = Arrays.stream(parameterString.split(" ")).filter(s -> !s.isEmpty())
					.toArray(String[]::new);
			if (parameterList.length >= 1) {
				modifier.setParameterList(parameterList);
			}
		}

		return modifier;
	}

	// ----- EXCECUTING METHODS ----- //
	private void createChannel(MessageReceivedEvent event, String name, List<IUser> allowedUsers, List<IUser> movePlayers,
			int limit, int timeout) {
		IGuild guild = event.getGuild();
		IUser owner = event.getAuthor();

		// create the new channel
		IVoiceChannel channel = guild.createVoiceChannel(name);

		// put channel to temp category
		moveChannelToTempCategory(guild, channel);

		// set user limit
		channel.changeUserLimit(limit);

		// add temp-Channel
		TempChannel tempChannel = new TempChannel(channel, owner, timeout);
		tempChannelsByGuild.get(guild).addTempChannel(tempChannel);

		// set channel permissions
		setChannelPermissions(owner, allowedUsers, guild, channel);

		// move players into new channel
		movePlayersToChannel(movePlayers, channel);

	}

	private void movePlayersToChannel(List<IUser> playersToMove, IVoiceChannel channel) {

		for (IUser user : playersToMove) {
			// only move players who are in voice channels already
			if (user.getVoiceStateForGuild(channel.getGuild()).getChannel() != null)
				user.moveToVoiceChannel(channel);
		}
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

	private void moveChannelToTempCategory(IGuild guild, IVoiceChannel channel) {
		ICategory targetCategory = null;

		List<ICategory> temp_categories = guild.getCategoriesByName("Temporary Channel");
		if (!temp_categories.isEmpty()) {
			targetCategory = temp_categories.get(0);
		} else {
			targetCategory = guild.createCategory("Temporary Channel");
		}

		channel.changeCategory(targetCategory);
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
			e.printStackTrace();
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

	private boolean modifierListContainsModifierstring(String modifierString, List<Modifier> modifierList) {
		for (Modifier modifier : modifierList) {
			if (modifier.getModifierName().equals(modifierString)) {
				return true;
			}
		}

		return false;
	}

}
