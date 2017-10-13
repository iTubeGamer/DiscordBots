package de.maxkroner.implementation.privateBot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import de.maxkroner.implementation.Bot;
import de.maxkroner.ui.PrivateBotMenue;
import de.maxkroner.ui.UserInput;
import sx.blah.discord.api.events.Event;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelJoinEvent;
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelMoveEvent;
import sx.blah.discord.handle.obj.ICategory;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.handle.obj.Permissions;

public class PrivateBot extends Bot {
	private static final String token = "MzY3NjY1NzIwMDU1ODI0Mzg0.DL-0Pg.UkGtH2Y8xTCQbWDAmUqGJdykbW8";
	private static ArrayList<String> channelNames = new ArrayList<>();
	private static ArrayList<TempChannel> tempChannels;
	private static final EnumSet<Permissions> voice_connect = EnumSet.of(Permissions.VOICE_CONNECT);
	private static final EnumSet<Permissions> empty = EnumSet.noneOf(Permissions.class);
	private static final int USER_CHANNEL_LIMIT = 3;

	public PrivateBot(Scanner scanner, UserInput userInput) {
		super(token, new PrivateBotMenue(scanner, userInput));

	}

	@EventSubscriber
	public void onReady(ReadyEvent event) {
		super.onReady(event);
		readChannelNames();

		// start Channel-Timout
		tempChannels = new ArrayList<>();
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
		CheckTempChannel checkEvent = new CheckTempChannel(tempChannels, executor);
		executor.scheduleAtFixedRate(checkEvent, 1, 1, TimeUnit.MINUTES);
	}

	@EventSubscriber
	public void onUserVoiceChannelJoin(UserVoiceChannelJoinEvent event) {
		updateChannelTimeout(event.getVoiceChannel());
	}

	@EventSubscriber
	public void onUserVoiceChannelMove(UserVoiceChannelMoveEvent event) {
		updateChannelTimeout(event.getVoiceChannel());
	}

	private void updateChannelTimeout(IVoiceChannel voiceChannel) {
		for (TempChannel tempChannel : tempChannels) {
			if (tempChannel.getChannel().equals(voiceChannel)) {
				tempChannel.setEmptyMinuts(0);
				System.out.println("Channel Timeout for " + voiceChannel.getName() + " has been reset!");
			}
		}
	}

	@EventSubscriber
	public void onMessageReceivedEvent(MessageReceivedEvent event) {
		String message = event.getMessage().getContent();
		IChannel channel = event.getMessage().getChannel();

		// scan for messages in the following structure: command [-modifier
		// [paramter ...] ...] ...
		String command = message;
		String[] modifierStrings = new String[0];
		if (message.contains(" ")) {
			command = message.split(" ")[0];

			String allModifiersString = message.substring(message.indexOf(" ") + 1);
			if (allModifiersString.charAt(0) == '-') {
				modifierStrings = allModifiersString.split("-");
				modifierStrings = Arrays.stream(modifierStrings).map(String::trim).filter(s -> !s.isEmpty()).toArray(String[]::new);
				//List<Modifier> modifier = (List<Modifier>) Arrays.stream(modifierStrings).map(String::trim).filter(s -> !s.isEmpty()).map(s -> parseModifierFromString(s)).collect(Collectors.toList());
			}
		}

		switch (command) {
		case "!channel":
			parseChannelCommand(event, modifierStrings, channel);
			break;
		case "!kick":
			parseKickCommand(modifierStrings, channel);
			break;
		case "!channelclear":
			parseChannelClearCommand(event, modifierStrings);
			break;
		}
	}

	private void parseChannelClearCommand(MessageReceivedEvent event, String[] modifierStrings) {
		if(getUserChannelCount(event.getAuthor()) == 0){
			sendMessage(event.getAuthor() + ", you have no temporary channels at the moment!", event.getChannel(), false);
			return;
		}
		
		boolean forceDelete = false;
		for (String modifierString : modifierStrings) {
			Modifier modifier = parseModifierFromString(modifierString);
			
		}
		
		for (TempChannel tempChannel : tempChannels){
			if(tempChannel.getOwner().equals(event.getAuthor()) && (tempChannel.getChannel().getConnectedUsers().isEmpty() || forceDelete)){
				tempChannel.getChannel().delete();
			}
		}
		System.out.println("channels deleted!");
		
	}

	private void parseKickCommand(String[] modifiers, IChannel channel) {
		sendMessage("Kick available soon!", channel, false);
	}

	private void parseChannelCommand(MessageReceivedEvent event, String[] modifierStrings, IChannel channel) {
		//check if User-Channel-Count-Limit is reached
		if(getUserChannelCount(event.getAuthor()) >= USER_CHANNEL_LIMIT){
			sendMessage("Sorry " + event.getAuthor().getName() + ", but you reached the personal channel limit of " + 
		USER_CHANNEL_LIMIT + ". Use !channelclear to delete all of your empty temporary channels. " + 
					"With the modifier -f you can force to delte all channels, even those who aren't empty!", event.getChannel(), false);
		return;
		}
		
		
		String name = getRandomName();
		List<IUser> allowedUsers = null; // null = everyone allowed
		boolean move = false;
		int limit = 0;
		int timeout = 5;

		for (String modifierString : modifierStrings) {
			Modifier modifier = parseModifierFromString(modifierString);

			switch (modifier.getModifierName()) {
			case "p":
				allowedUsers = parsePrivateModifier(modifier, event, allowedUsers);
				break;
			case "m":
				move = true;
				if (modifier.getParameterList().length > 0) {
					sendMessage(
							"You don't need to specify any parameters for the move-modifier (-m). You can, but you will get this message everytime you do :wink:",
							event.getChannel(), false);
				}
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
							event.getChannel(), false);
				}
				break;
			case "t":
				if (modifier.getParameterList().length >= 1) {
					int given_limit = Integer.parseInt(modifier.getParameterList()[0]);
					if (given_limit > 0 && given_limit <= 60) {
						limit = given_limit;
					} else {
						sendMessage(
								"Please use the timout-modifier (-t) only with a parameter between 1-60 minutes (f.e. !channel -t 5)",
								event.getChannel(), false);
					}
				} else {
					sendMessage(
							"Please use the timout-modifier (-t) only with a parameter between 1-60 (f.e. !channel -t 5)",
							event.getChannel(), false);
				}

				break;
			}
		}

		createChannel(event, name, allowedUsers, move, limit, timeout);

	}

	private int getUserChannelCount(IUser user) {
		int channelCount = 0;
		for (TempChannel tempChannel : tempChannels){
			if (tempChannel.getOwner().equals(user)){
				channelCount += 1;
			}
		}
		return channelCount;
	}

	private List<IUser> parsePrivateModifier(Modifier modifier, MessageReceivedEvent event, List<IUser> allowedUsers) {
		// no users metioned
		if (modifier.getParameterList().length <= 0) {
			sendMessage(
					"You need to specify the users who may join your private channel, when using the private-modificator (-p). (f.e. !channel -p @user1 @user2",
					event.getChannel(), false);
			return allowedUsers;
		}

		allowedUsers = new ArrayList<IUser>();

		// all users allowed
		if (modifier.getParameterList()[0].equals("all")) {
			allowedUsers = event.getAuthor().getVoiceStateForGuild(event.getGuild()).getChannel().getConnectedUsers();
			if (!allowedUsers.contains(event.getAuthor())) {
				allowedUsers.add(event.getAuthor());
			}
			return allowedUsers;
		}

		// users mentioned by name
		List<String> notFound = new ArrayList<>();
		for (String parameter : modifier.getParameterList()) {
			// users metioned by snowflake-ID
			if (parameter.startsWith("<@!")) {
				IUser userByID = event.getGuild()
						.getUserByID(Long.parseLong(parameter.substring(3, parameter.length() - 1)));
				if (userByID != null) {
					allowedUsers.add(userByID);
					break;
				}
			}

			// users mentioned by User/-Nickname
			List<IUser> usersByParameter = event.getGuild().getUsersByName(parameter, true);
			if (!usersByParameter.isEmpty()) {
				allowedUsers.addAll(usersByParameter);
				break;
			}

			notFound.add(parameter);
		}

		if (!notFound.isEmpty()) {
			String not_found = "";
			for (String username : notFound) {
				not_found = not_found + username + ", ";
			}

			not_found = not_found.substring(0, not_found.length() - 2);

			sendMessage("The following Usernames were not found: " + not_found, event.getChannel(), false);
		}

		return allowedUsers;
	}

	private void createChannel(MessageReceivedEvent event, String name, List<IUser> allowedUsers, boolean move,
			int limit, int timeout) {
		IGuild guild = event.getGuild();
		IUser author = event.getAuthor();

		// create the new channel
		IVoiceChannel channel = guild.createVoiceChannel(name);

		// put channel in fitting category
		List<ICategory> categories = guild.getCategories();
		ICategory targetCategory = null;
		for (ICategory category : categories) {
			if (category.getName().contains("temp")) {
				targetCategory = category;
				break;
			}
		}
		channel.changeCategory(targetCategory);

		// set userlimit
		channel.changeUserLimit(limit);

		// add temp-Channel
		tempChannels.add(new TempChannel(channel, event.getAuthor(), timeout));

		// set channel rights
		// channel owner can do everything
		channel.overrideUserPermissions(event.getAuthor(), EnumSet.allOf(Permissions.class), empty);
		if (allowedUsers != null) {
			// allowedUsers may connect
			for (IUser user : allowedUsers) {
				channel.overrideUserPermissions(user, voice_connect, empty);
			}
			// everyone else may not connect
			channel.overrideRolePermissions(guild.getEveryoneRole(), empty, voice_connect);
		}

		// move players into new channel
		if (move) {
			List<IUser> playersToMove;
			if (allowedUsers != null) {
				// move all players into the channel who are allowed
				playersToMove = allowedUsers;
			} else {
				// if all players are allowed: move all players in same channel
				playersToMove = author.getVoiceStateForGuild(guild).getChannel().getConnectedUsers();
			}

			for (IUser user : playersToMove) {
				// only move players who are in voice channels already
				if (user.getVoiceStateForGuild(guild).getChannel() != null)
					user.moveToVoiceChannel(channel);
			}
		}

	}

	private String getRandomName() {
		int index = ThreadLocalRandom.current().nextInt(0, channelNames.size());

		return channelNames.get(index);
	}

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

}
