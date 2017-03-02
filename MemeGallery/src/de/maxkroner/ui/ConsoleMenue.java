package de.maxkroner.ui;

import java.util.List;
import java.util.Scanner;

import de.maxkroner.enums.FileFormat;
import de.maxkroner.implementation.JokeBot;
import de.maxkroner.main.Main;
import de.maxkroner.reader.FlachwitzUrlReader;
import de.maxkroner.reader.JokeFileReader;
import die.maxkroner.database.JokeDatabase;
import sx.blah.discord.handle.obj.IGuild;

public class ConsoleMenue {
	private JokeBot bot;
	private UserInput userinput;
	private Scanner scanner;
	private JokeDatabase jokeDatabase;

	public ConsoleMenue(UserInput userinput, Scanner scanner, JokeDatabase jokeDatabase) {
		super();
		this.userinput = userinput;
		this.scanner = scanner;
		this.jokeDatabase = jokeDatabase;
	}

	public void startMenue(JokeBot jokebot) {
		bot = jokebot;
		int auswahl = 0;
		while (!(auswahl == 4)) {

			auswahl = userinput.getMultipleChoiceResult("What to do?", "manage database", "reset bot nickname",
					"print jokes from database", "shut down bot");

			switch (auswahl) {
			case 1:
				manageDatabase();
				break;
			case 2:
				resetNickname();
				break;
			case 3:
				printJokes();
				break;
			case 4:
				if(!userinput.getYesNoResult("Are you sure?")){
					auswahl = 1;
				}
			}

		}
		Main.exit();

	}

	private void resetNickname() {
		List<IGuild> guilds = bot.getClient().getGuilds();
		IGuild guild = (IGuild) userinput.getMultipleChoiceResult("Which guild?", guilds);
		bot.resetNickname(guild);
	}

	private void printJokes() {
		if (userinput.getYesNoResult("Select a category?")) {
			String category;
			category = (String) userinput.getMultipleChoiceResult("Which category?", jokeDatabase.getJokeCategories());

			System.out.println("ok, printing all jokes of category " + category);
			jokeDatabase.printAllJokes(category);
		} else {
			jokeDatabase.printAllJokes();
		}
	}

	private void manageDatabase() {
		int auswahl = userinput.getMultipleChoiceResult("Which database change did you think of?", "reset database",
				"import jokes from file", "import Flachwitze", "never mind");

		switch (auswahl) {
		case 1:
			if (userinput.getYesNoResult("Really? All Jokes will be deleted!")) {
				jokeDatabase.createTable();
				System.out.println("Database has been reset.");
			}
			break;
		case 2:
			readFromFile(scanner);
			break;
		case 3:
			readFlachwitze(scanner);

		}
	}

	private void readFlachwitze(Scanner scanner) {
		// How many to read?
		System.out.println("How many?");
		int anzahl = scanner.nextInt();

		// Delete existing Flachwitze?
		if (userinput.getYesNoResult("Delete existing Flachwitze?")) {
			jokeDatabase.deleteCategory("flach");
		}

		// read jokes
		FlachwitzUrlReader reader = new FlachwitzUrlReader();
		List<String> jokes = reader.getJokes(anzahl);

		// write jokes to database
		jokeDatabase.insertJokes(jokes, "flach");

		// print all Flachwitze?
		if (userinput.getYesNoResult("Print all read Flachwitze?")) {
			jokeDatabase.printAllJokes("flach");
		}

		// update joke categories
		bot.updateJokeCategories();
	}

	private void readFromFile(Scanner scanner) {
		// get path to file
		System.out.println("Please insert path to file.");
		String path = scanner.next() + scanner.nextLine();

		// get format of file
		Enum<?> format = userinput.getMultipleChoiceResult("How is the file formatted?", FileFormat.ONE_PER_LINE,
				FileFormat.SEPERATED_BY_FREE_LINE);

		// get joke category
		System.out.println("Which joke category?");
		String category = scanner.next();

		// read jokes, print if chosen
		Boolean printAllJokes = userinput.getYesNoResult("Print all read jokes?");
		List<String> jokes = JokeFileReader.getJokes(path, (FileFormat) format, printAllJokes);

		// write jokes to database
		jokeDatabase.insertJokes(jokes, category);

		// update joke categories
		bot.updateJokeCategories();

	}
}
