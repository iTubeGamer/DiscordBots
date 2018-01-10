package de.maxkroner.reader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import de.maxkroner.enums.FileFormat;

public class JokeFileReader {

	public static List<String> getJokes(String fileName, FileFormat format, Boolean printJokes) {
		List<String> jokes = new ArrayList<String>();

		switch (format) {
		case ONE_PER_LINE:
			readJokesOnePerLine(fileName, jokes);
			break;
		case SEPERATED_BY_FREE_LINE:
			readJokesSeparatedByFreeLine(fileName, jokes);
		}

		if (printJokes)
			jokes.forEach(System.out::println);

		return jokes;
	}

	private static void readJokesOnePerLine(String fileName, List<String> jokes) {

		try (Scanner scanner = new Scanner(new File(fileName))) {

			while (scanner.hasNext()) {
				jokes.add(scanner.nextLine());
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void readJokesSeparatedByFreeLine(String fileName, List<String> jokes) {

		try (Scanner scanner = new Scanner(new File(fileName))) {

			String joke = "";
			String nextLine;
			while (scanner.hasNext()) {
				nextLine = scanner.nextLine();
				if (!nextLine.equals("") && !nextLine.equals(" ")) {
					if (joke.length() == 0) {
						joke = nextLine;
					} else {
						joke = joke + "\n" + nextLine;
					}
				} else {
					jokes.add(joke);
					joke = "";
				}

			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
