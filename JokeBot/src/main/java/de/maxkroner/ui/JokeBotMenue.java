package de.maxkroner.ui;

import java.util.List;
import java.util.Scanner;

import de.maxkroner.database.JokeDatabase;
import de.maxkroner.enums.FileFormat;
import de.maxkroner.implementation.Bot;
import de.maxkroner.implementation.JokeBot;
import de.maxkroner.reader.FlachwitzUrlReader;
import de.maxkroner.reader.JokeFileReader;

public class JokeBotMenue extends ConsoleMenue {
	private JokeDatabase jokeDatabase;

	public JokeBotMenue(Bot bot, JokeDatabase jokeDatabase) {
		super(bot);
		this.jokeDatabase = jokeDatabase;
	}

	public JokeDatabase getJokeDatabase() {
		return jokeDatabase;
	}

	public void startMenue(Bot bot) {
		int auswahl = 0;
		while (!(auswahl == 5 | auswahl == 4)) {

			auswahl = ConsoleUserInput.getMultipleChoiceResult("What to do?", "manage database", "configure bot", "print jokes from database",
					"leave menue, but keep bot running", "shut down bot");

			switch (auswahl) {
			case 1:
				manageDatabase();
				break;
			case 2:
				customizeBot();
				break;
			case 3:
				printJokes();
				break;
			case 4:
				if (!ConsoleUserInput.getYesNoResult("Are you sure?")) {
					auswahl = 1;
				}
				break;
			case 5:
				if (ConsoleUserInput.getYesNoResult("Are you sure?")) {
					bot.disconnect();
					System.exit(0);
				} else {
					auswahl = 1;
				}
			}
		}

	}

	private void printJokes() {
		List<String> categories = jokeDatabase.getJokeCategories();
		if (!categories.isEmpty() && ConsoleUserInput.getYesNoResult("Select a category?")) {
			String chosenCategory;
			chosenCategory = (String) ConsoleUserInput.getMultipleChoiceResult("Which category?", categories, Object::toString);

			System.out.println("ok, printing all jokes of category " + chosenCategory);
			jokeDatabase.printAllJokes(chosenCategory);
		} else {
			jokeDatabase.printAllJokes();
		}
	}

	private void manageDatabase() {
		int auswahl = ConsoleUserInput.getMultipleChoiceResult("Which database change did you think of?", "reset database", "import jokes from file",
				"import Flachwitze", "never mind");

		switch (auswahl) {
		case 1:
			if (ConsoleUserInput.getYesNoResult("Really? All Jokes will be deleted!")) {
				jokeDatabase.createTable();
				System.out.println("Database has been reset.");
			}
			break;
		case 2:
			readFromFile();
			break;
		case 3:
			readFlachwitze();

		}
	}

	private void readFlachwitze() {
		Scanner scanner = new Scanner(System.in);
		// How many to read?
		System.out.println("How many?");
		int anzahl = scanner.nextInt();

		// Delete existing Flachwitze?
		if (ConsoleUserInput.getYesNoResult("Delete existing Flachwitze?")) {
			jokeDatabase.deleteCategory("flach");
		}

		// read jokes
		FlachwitzUrlReader reader = new FlachwitzUrlReader();
		List<String> jokes = reader.getJokes(anzahl);

		// write jokes to database
		jokeDatabase.insertJokes(jokes, "flach");

		// print all Flachwitze?
		if (ConsoleUserInput.getYesNoResult("Print all read Flachwitze?")) {
			jokeDatabase.printAllJokes("flach");
		}

		// update joke categories
		JokeBot jokeBot = (JokeBot) bot;
		jokeBot.updateJokeCategories();
		scanner.close();
	}

	private void readFromFile() {
		Scanner scanner = new Scanner(System.in);
		// get path to file
		System.out.println("Please insert path to file.");
		String path = scanner.next() + scanner.nextLine();

		// get format of file
		Enum<?> format = ConsoleUserInput.getMultipleChoiceResult("How is the file formatted?", FileFormat.ONE_PER_LINE, FileFormat.SEPERATED_BY_FREE_LINE);

		// get joke category
		System.out.println("Which joke category?");
		String category = scanner.next();

		// read jokes, print if chosen
		Boolean printAllJokes = ConsoleUserInput.getYesNoResult("Print all read jokes?");
		List<String> jokes = JokeFileReader.getJokes(path, (FileFormat) format, printAllJokes);

		// write jokes to database
		jokeDatabase.insertJokes(jokes, category);

		// update joke categories
		JokeBot jokeBot = (JokeBot) bot;
		jokeBot.updateJokeCategories();
		scanner.close();
	}

}
