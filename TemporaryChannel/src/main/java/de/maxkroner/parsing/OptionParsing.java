package de.maxkroner.parsing;

import java.util.ArrayList;
import java.util.List;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.obj.User;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IDiscordObject;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.MessageTokenizer;
import sx.blah.discord.util.MessageTokenizer.MentionToken;

public class OptionParsing {
	
	public static List<IUser> parsePrivateOption(CommandOption option, MessageReceivedEvent event, List<String> errorMessages, IDiscordClient client) {
		// no users mentioned = no private channel
		List<IUser> allowedUsers = new ArrayList<>();
		if (option.getParameterList().length <= 0) {
			return allowedUsers;
		}else if (option.getParameterList()[0].equals("all") && option.getParameterList().length == 1) {
			// all users in current channel are allowed
			allowedUsers = event.getAuthor().getVoiceStateForGuild(event.getGuild()).getChannel().getConnectedUsers();
			allowedUsers.remove(event.getAuthor());
			if (allowedUsers.isEmpty()) {
				allowedUsers = null; // none of the mentioned users were recognized -> channel will not be private
			}
			return allowedUsers;
		}

		// users mentioned by name
		parseUserList(option, event, allowedUsers, errorMessages, client);
		return allowedUsers;
	}

	public static void parseUserList(CommandOption option, MessageReceivedEvent event, List<IUser> userList, List<String> errorMessages, IDiscordClient client) {
		List<String> notFound = new ArrayList<>();
		for (String parameter : option.getParameterList()) {
			parseUserParameter(event.getGuild(), userList, notFound, parameter, client);
		}

		if (!notFound.isEmpty()) {
			String not_found = "";
			for (String username : notFound) {
				not_found = not_found + username + ", ";
			}

			not_found = not_found.substring(0, not_found.length() - 2);
			errorMessages.add("The following usernames were not found: " + not_found);
		}
	}

	private static void parseUserParameter(IGuild guild, List<IUser> allowedUsers, List<String> notFound, String parameter, IDiscordClient client) {
		// user mentioned by snowflake-ID
		IUser user;
		MessageTokenizer mt = new MessageTokenizer(client, parameter);
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
	
	public static List<IUser> parseMoveOption(List<CommandOption> options, CommandOption option, MessageReceivedEvent event, List<IUser> movePlayers, List<String> errorMessages, IDiscordClient client) {
		/*
			boolean privateOptionUsed = optionListContainsOptionString("p", options);
			
			if (option.getParameterList().length > 0 && privateOptionUsed) {
				sendMessage(
						"Tip: If you create a private channel with -p and you don't mention users behind -m, all users you mentioned behind -p will be moved automatically.",
						event.getChannel(), false);
			} else 
			
			//all alowed users will be moved
			if (option.getParameterList().length == 0 && privateOptionUsed) {
				return null;
			}
			*/
			
			if (option.getParameterList().length == 0) {
				// move only author
				movePlayers = new ArrayList<IUser>();
				movePlayers.add(event.getAuthor());
			} else if (option.getParameterList()[0].equals("all")) { 
				//all users in current channel should be moved
				if ((event.getAuthor().getVoiceStateForGuild(event.getGuild()).getChannel().getConnectedUsers() != null)) {
					movePlayers = event.getAuthor().getVoiceStateForGuild(event.getGuild()).getChannel().getConnectedUsers();
				}
			} else {
				// users mentioned by name
				parseUserList(option, event, movePlayers, errorMessages, client);
			}
	
			return movePlayers;
		}

	private static boolean optionListContainsOptionString(String optionString, List<CommandOption> options) {
		for (CommandOption option : options) {
			if (option.getCommandOptionName().equals(optionString)) {
				return true;
			}
		}
	
		return false;
	}
	
	public static String parseNameOption(IChannel channel, String name, CommandOption option, List<String> errorMessages) {
		if (option.getParameterList().length >= 1) {
			name = "";
			for (String parameter : option.getParameterList()) {
				name = name + " " + parameter;
			}

			name = name.substring(1);

		} else {
			errorMessages.add("The name-option `-n` has to be used with a parameter to specify the channel-name: `!c -n channel_title`");
		}
		return name;
	}
	
	public static int parseLimitOption(IChannel channel, int limit, CommandOption option, List<String> errorMessages) {
		if (option.getParameterList().length >= 1) {
			int given_limit = Integer.parseInt(option.getParameterList()[0]);
			if (given_limit >= 1 && given_limit <= 99) {
				limit = given_limit;
			} else {
				errorMessages.add("The user-limit-option `-l` has to be used with a limit between 1 and 99: `!c -l 5`");
			}
		} else {
			errorMessages.add("The user-limit-option `-l` has to be used with a limit between 1 and 99: `!c -l 5`");
		}
		return limit;
	}
	
	public static int parseTimoutOption(IChannel channel, int timeout, CommandOption option, List<String> errorMessages) {
		if (option.getParameterList().length >= 1) {
			int given_timeout = Integer.parseInt(option.getParameterList()[0]);
			if (given_timeout >= 1 && given_timeout <= 180) {
				timeout = given_timeout;
			} else {
				errorMessages.add("The timeout-option `-t` has to be used with a timeout between 1-180: `!c -t 5`");
			}
		} else {
			errorMessages.add("The timeout-option `-t` has to be used with a timeout between 1-180: `!c -t 5`");
		}
		return timeout;
	}

}
