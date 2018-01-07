package de.maxkroner.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import de.maxkroner.implementation.Bot;
import de.maxkroner.implementation.privateBot.TempChannel;
import de.maxkroner.implementation.privateBot.TempChannelMap;
import de.maxkroner.main.Main;
import io.bretty.console.table.Alignment;
import io.bretty.console.table.ColumnFormatter;
import io.bretty.console.table.Precision;
import io.bretty.console.table.Table;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;

public class PrivateBotMenue extends BotMenue {
	private HashMap<IGuild, TempChannelMap> tempChannelsByGuild;

	public PrivateBotMenue(Scanner scanner, UserInput userInput,
			HashMap<IGuild, TempChannelMap> tempChannelsByGuild) {
		super(scanner, userInput);
		this.tempChannelsByGuild = tempChannelsByGuild;
	}

	public void startMenue(Bot bot) {
		super.startMenue(bot);

		int auswahl = 0;
		while (!(auswahl == 4 | auswahl == 3)) {

			auswahl = userInput.getMultipleChoiceResult("What to do?", "print current state",
					"configure bot", "leave menue, but keep bot running", "shut down bot");

			switch (auswahl) {
			case 1:
				printState();
				break;
			case 2:
				customizeBot();
				break;
			case 3:
				if (!userInput.getYesNoResult("Are you sure?")) {
					auswahl = 1;
				}
				break;
			case 4:
				if (userInput.getYesNoResult("Are you sure?")) {
					bot.disconnect();
				}
			}
		}
	}

	private void printState() {
		IGuild chosenGuild = null;

		switch (tempChannelsByGuild.keySet().size()) {
		case 0:
			System.out.println("The bot isn't connected to any guilds.");
			break;
		case 1:
			Iterator<IGuild> iterator = tempChannelsByGuild.keySet().iterator();
			chosenGuild = iterator.next();
			break;
		default:
			List<IGuild> guilds = new ArrayList<>(tempChannelsByGuild.keySet());
			chosenGuild = userInput.getMultipleChoiceResult("For which guild?", guilds, IGuild::getName);
		}

		if (chosenGuild != null) {
			// define formatter for each column
			ColumnFormatter<String> stringFormatter = ColumnFormatter.text(Alignment.CENTER, 20);
			ColumnFormatter<Number> twoDigitNumberFormatter = ColumnFormatter.number(Alignment.CENTER, 20,
					Precision.ZERO);
			ColumnFormatter<Number> threeDigitNumberFormatter = ColumnFormatter.number(Alignment.CENTER, 20,
					Precision.ZERO);

			//get table content
			String[] channelNames = tempChannelsByGuild.get(chosenGuild).getAllTempChannel().stream()
																							.map(TempChannel::getChannel)
																							.map(IVoiceChannel::getName)
																							.toArray(String[]::new);
			
			String[] channelOwner = tempChannelsByGuild.get(chosenGuild).getAllTempChannel().stream()
					.map(TempChannel::getOwner)
					.map(IUser::getName)
					.toArray(String[]::new);
			
			Integer[] channelLimits = tempChannelsByGuild.get(chosenGuild).getAllTempChannel().stream()
					.map(TempChannel::getChannel)
					.map(IVoiceChannel::getUserLimit)
					.toArray(Integer[]::new);
			
			Integer[] timeoutValues = tempChannelsByGuild.get(chosenGuild).getAllTempChannel().stream()
					.map(TempChannel::getTimeoutInMinutes)
					.toArray(Integer[]::new);
			
			Integer[] emptyValues = tempChannelsByGuild.get(chosenGuild).getAllTempChannel().stream()
					.map(TempChannel::getEmptyMinuts)
					.toArray(Integer[]::new);
			

			// create a builder with first column (name column)
			Table.Builder builder = new Table.Builder("Temporary Channel", channelNames, stringFormatter);

			// add other columns
			builder.addColumn("owner", channelOwner, stringFormatter);
			builder.addColumn("user-limit", channelLimits, twoDigitNumberFormatter);
			builder.addColumn("timeout", timeoutValues, threeDigitNumberFormatter);
			builder.addColumn("empty minutes", emptyValues, threeDigitNumberFormatter);

			// build the table and print it
			Table table = builder.build();
			System.out.println(table);
		}

	}

}
