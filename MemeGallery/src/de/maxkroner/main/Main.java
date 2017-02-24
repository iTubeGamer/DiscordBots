package de.maxkroner.main;

import java.util.List;
import java.util.Scanner;

import de.maxkroner.enums.FileFormat;
import de.maxkroner.implementation.JokeBot;
import de.maxkroner.reader.FlachwitzUrlReader;
import de.maxkroner.reader.JokeFileReader;
import de.maxkroner.ui.UserInput;

public class Main {

	private static Scanner scanner;

	public static void main(String[] args) {
		scanner = new Scanner(System.in);

		startBot(args);
		JokeDatabase.connect();
		startMenue();

	}

	private static void startBot(String[] args) {
		if (args.length < 1) { // Needs a bot token provided
			scanner.close();
			throw new IllegalArgumentException("Please provide the Bot-Token as argument!");
		}
		JokeBot bot = new JokeBot(args[0]);
	}

	private static void startMenue() {
		int auswahl = -1;
		while (!(auswahl == 3)) {
			System.out.println("What to do?");
			System.out.println("(0) reset database");
			System.out.println("(1) read Flachwitze");
			System.out.println("(2) read from file");
			System.out.println("(3) exit");
			auswahl = scanner.nextInt();

			switch (auswahl) {
			case 0:
				JokeDatabase.createTable();
				System.out.println("Done.");
				break;
			case 1:
				readFlachwitze(scanner);
				break;
			case 2:
				readFromFile(scanner);
			}

		}
		exit();

	}

	private static void exit() {
		JokeDatabase.disconnect();
		scanner.close();
		System.out.println("finished");
		System.exit(0);
	}

	private static void readFlachwitze(Scanner scanner) {
		// How many to read?
		System.out.println("How many?");
		int anzahl = scanner.nextInt();

		// Delete existing Flachwitze?
		if (UserInput.getYesNoResult("Delete existing Flachwitze?", scanner)) {
			JokeDatabase.deleteCategory("flach");
		}

		// read jokes
		FlachwitzUrlReader reader = new FlachwitzUrlReader();
		List<String> jokes = reader.getJokes(anzahl);

		// write jokes to database
		JokeDatabase.insertJokes(jokes, "flach");

		// print all Flachwitze?
		if (UserInput.getYesNoResult("Print all read Flachwitze?", scanner)) {
			JokeDatabase.printAllJokes();
		}
	}

	private static void readFromFile(Scanner scanner) {
		// get path to file
		System.out.println("Please insert path to file.");
		String path = scanner.next() + scanner.nextLine();

		// get format of file
		Enum format = UserInput.getMultipleChoiceResult("How is the file formatted?", scanner, FileFormat.ONE_PER_LINE,
				FileFormat.SEPERATED_BY_FREE_LINE);

		// get joke category
		System.out.println("Which joke category?");
		String category = scanner.next();

		// read jokes, print if chosen
		Boolean printAllJokes = UserInput.getYesNoResult("Print all read jokes?", scanner);
		List<String> jokes = JokeFileReader.getJokes(path, (FileFormat) format, printAllJokes);

		// write jokes to database
		JokeDatabase.insertJokes(jokes, category);

	}

}
