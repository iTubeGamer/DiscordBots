package de.maxkroner.parsing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.obj.User;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IDiscordObject;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.MessageTokenizer;
import sx.blah.discord.util.MessageTokenizer.MentionToken;

public class OptionParsing {

	public static List<IUser> parsePrivateOption(CommandOption option, MessageReceivedEvent event, IDiscordClient client) {

		if (option.getParameterList().size() <= 0) {
			return Collections.emptyList();
		} else if (option.getParameterList().get(0).equals("all") && option.getParameterList().size() == 1) {
			// all users in current channel are allowed
			List<IUser> allowedUsers = event.getAuthor().getVoiceStateForGuild(event.getGuild()).getChannel().getConnectedUsers();
			allowedUsers.remove(event.getAuthor());
			if (allowedUsers.isEmpty()) {
				allowedUsers = null; // none of the mentioned users were recognized -> channel will not be private
			}
			return allowedUsers;
		} else {
			// users mentioned by name
			return parseUserList(option.getParameterList(), client);
		}
	}

	public static List<IUser> parseUserList(List<String> userStrings, IDiscordClient client) {

		return userStrings.stream().map(T -> parseUserFromString(T, client)).filter(Optional::isPresent).map(Optional::get)
				.collect(Collectors.toList());
	}

	public static Optional<IUser> parseUserFromString(String userString, IDiscordClient client) {
		// user mentioned by snowflake-ID
		IUser user;
		MessageTokenizer mt = new MessageTokenizer(client, userString);
		if (mt.hasNextMention()) {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			MentionToken<IDiscordObject> nextMention = mt.nextMention();
			if (nextMention.getMentionObject().getClass() == User.class) {
				user = (IUser) nextMention.getMentionObject();
				return Optional.of(user);
			}
		}
		return Optional.empty();
	}

	public static List<IUser> parseMoveOption(CommandOption option, MessageReceivedEvent event, IDiscordClient client) {
		List<IUser> movePlayers = null;
		if (option.getParameterList().size() == 0) {
			// move only author
			movePlayers = new ArrayList<>();
			movePlayers.add(event.getAuthor());
		} else if (option.getParameterList().get(0).equals("all")) {
			// all users in current channel should be moved
			if ((event.getAuthor().getVoiceStateForGuild(event.getGuild()).getChannel().getConnectedUsers() != null)) {
				movePlayers = event.getAuthor().getVoiceStateForGuild(event.getGuild()).getChannel().getConnectedUsers();
			}
		} else {
			// users mentioned by name
			movePlayers = parseUserList(option.getParameterList(), client);
		}

		return movePlayers;
	}

	public static String parseNameOption(IChannel channel, String name, CommandOption option, List<String> errorMessages) {
		if (option.getParameterList().size() >= 1) {
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
		if (option.getParameterList().size() >= 1) {
			int given_limit = Integer.parseInt(option.getParameterList().get(0));
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
		if (option.getParameterList().size() >= 1) {
			int given_timeout = Integer.parseInt(option.getParameterList().get(0));
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
