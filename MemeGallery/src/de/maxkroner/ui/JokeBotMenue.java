package de.maxkroner.ui;

import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

import de.maxkroner.database.JokeDatabase;
import de.maxkroner.enums.FileFormat;
import de.maxkroner.implementation.Bot;
import de.maxkroner.implementation.JokeBot;
import de.maxkroner.main.Main;
import de.maxkroner.reader.FlachwitzUrlReader;
import de.maxkroner.reader.JokeFileReader;

public class JokeBotMenue extends BotMenue {
	private JokeDatabase jokeDatabase;

	public JokeBotMenue(Scanner scanner, UserInput userInput, JokeDatabase jokeDatabase) {
		super(scanner, userInput);
		this.jokeDatabase = jokeDatabase;
	}

	public JokeDatabase getJokeDatabase() {
		return jokeDatabase;
	}

	public UserInput getUserinput() {
		return userInput;
	}

	public Scanner getScanner() {
		return scanner;
	}

	public void startMenue(Bot bot) {
		super.startMenue(bot);
		
				int auswahl = 0;
				while (!(auswahl == 5 | auswahl == 4)) {

					auswahl = userInput.getMultipleChoiceResult("What to do?", "manage database", "configure bot",
							"print jokes from database", "leave menue, but keep bot running", "shut down bot");

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
						if (!userInput.getYesNoResult("Are you sure?")) {
							auswahl = 1;
						}
						break;
					case 5:
						if (userInput.getYesNoResult("Are you sure?")) {
							bot.disconnect();;
						}
					}
				}

			
	}

	private void printJokes() {
		List<String> categories = jokeDatabase.getJokeCategories();
		if (!categories.isEmpty() && userInput.getYesNoResult("Select a category?")) {
			String chosenCategory;
			chosenCategory = (String) userInput.getMultipleChoiceResult("Which category?", categories, Object::toString);

			System.out.println("ok, printing all jokes of category " + chosenCategory);
			jokeDatabase.printAllJokes(chosenCategory);
		} else {
			jokeDatabase.printAllJokes();
		}
	}

	private void manageDatabase() {
		int auswahl = userInput.getMultipleChoiceResult("Which database change did you think of?", "reset database",
				"import jokes from file", "import Flachwitze", "never mind");

		switch (auswahl) {
		case 1:
			if (userInput.getYesNoResult("Really? All Jokes will be deleted!")) {
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
		if (userInput.getYesNoResult("Delete existing Flachwitze?")) {
			jokeDatabase.deleteCategory("flach");
		}

		// read jokes
		FlachwitzUrlReader reader = new FlachwitzUrlReader();
		List<String> jokes = reader.getJokes(anzahl);

		// write jokes to database
		jokeDatabase.insertJokes(jokes, "flach");

		// print all Flachwitze?
		if (userInput.getYesNoResult("Print all read Flachwitze?")) {
			jokeDatabase.printAllJokes("flach");
		}

		// update joke categories
		JokeBot jokeBot = (JokeBot) bot;
		jokeBot.updateJokeCategories();
	}

	private void readFromFile(Scanner scanner) {
		// get path to file
		System.out.println("Please insert path to file.");
		String path = scanner.next() + scanner.nextLine();

		// get format of file
		Enum<?> format = userInput.getMultipleChoiceResult("How is the file formatted?", FileFormat.ONE_PER_LINE,
				FileFormat.SEPERATED_BY_FREE_LINE);

		// get joke category
		System.out.println("Which joke category?");
		String category = scanner.next();

		// read jokes, print if chosen
		Boolean printAllJokes = userInput.getYesNoResult("Print all read jokes?");
		List<String> jokes = JokeFileReader.getJokes(path, (FileFormat) format, printAllJokes);

		// write jokes to database
		jokeDatabase.insertJokes(jokes, category);

		// update joke categories
		JokeBot jokeBot = (JokeBot) bot;
		jokeBot.updateJokeCategories();

	}

}
