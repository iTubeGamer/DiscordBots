package de.maxkroner.implementation;

import java.util.List;
import java.util.Optional;

import de.maxkroner.database.JokeDatabase;
import de.maxkroner.ui.JokeBotMenue;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.obj.User;
import sx.blah.discord.handle.obj.IChannel;


public class JokeBot extends Bot {
	private List<String> jokeCategories;
	private JokeDatabase jokeDatabase;

	public JokeBot(String token) {
		super("JokeBot");
		JokeBotMenue jbMenue = new JokeBotMenue(new JokeDatabase());
		super.addConsoleMenue(jbMenue);
		super.addLogging("jb");
		super.run(token);
		this.jokeDatabase = jbMenue.getJokeDatabase();
	}

	private void tellJoke(String category, IChannel channel) {
		if (category.equals("random")){
			String joke = jokeDatabase.getRandomJoke(Optional.empty());
			sendMessage(joke, channel, true);
		}
		else if (jokeCategories.contains(category)) {

			String joke = jokeDatabase.getRandomJoke(Optional.of(category));
			sendMessage(joke, channel, true);

		} else {
			sendMessage("I dont know the category '" + category + "' yet.", channel, false);
		}
	}

	private void tellCategories(IChannel channel) {
		String categories = "If you use `!joke` `[category]` i will tell you a joke from the specified category.\n"
				+ "These are the categories I know so far:\n\n";

		for (String category : jokeCategories) {
			categories = categories + "`" + category + "`" + "\n";
		}

		categories = categories + "Use `random` for a joke from any category.";
		
		sendMessage(categories, channel, false);
	}

	@EventSubscriber
	public void onReady(ReadyEvent event) {
		super.onReady(event);
		updateJokeCategories();
	}

	public void updateJokeCategories() {
		jokeCategories = jokeDatabase.getJokeCategories();

	}

	@EventSubscriber
	public void onMessageReceivedEvent(MessageReceivedEvent event) {
		String message = event.getMessage().getContent();
		IChannel channel = event.getMessage().getChannel();
		if (message.startsWith("!change nick")) {
			User user = (User) getClient().getOurUser();
			channel.getGuild().setUserNickname(user, message.substring(13));
		} else if ((message.startsWith("!joke ") && (message.length() > 6))) {
			tellJoke(message.substring(message.indexOf(" ") + 1), channel);
		} else if (message.equals("!joke")) {
			tellCategories(channel);
		}
	}
	
	public void disconnect(){
		jokeDatabase.close();
	}
}
