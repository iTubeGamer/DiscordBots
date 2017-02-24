package de.maxkroner.ui;

import java.util.Scanner;

public abstract class UserInput {
	/**
	 * Prints the provided question and returns the users answer as Boolean
	 * 
	 * @param question
	 *            question to ask the user
	 * @param scanner
	 *            Scanner object to use
	 * @return users answer
	 */
	public static Boolean getYesNoResult(String question, Scanner scanner) {
		String answer = "";
		while (!answer.equals("y") && !answer.equals("n")) {
			System.out.println(question + " (y/n)");
			answer = scanner.next();
		}

		return answer.equals("y");
	}

	/**
	 * Prints the provided question and returns the users answer as Boolean
	 * 
	 * @param question
	 *            question to ask the user
	 * @return users answer
	 */
	public static Boolean getYesNoResult(String question) {
		Scanner scanner = new Scanner(System.in);
		return getYesNoResult(question, scanner);
	}

	/**
	 * Prints the provided question an the possible answers
	 * 
	 * @param question
	 *            the question to ask the user
	 * @param answers
	 *            the answers the user has to choose from
	 * @return int representing the answer choosen by the user, starting with 1
	 */
	public static int getMultipleChoiceResult(String question, Scanner scanner, String... answers) {
		int result = 0;

		while (result > answers.length | result < 1) {
			System.out.println(question);
			for (int i = 1; i <= answers.length; i++) {
				System.out.println("(" + i + ") " + answers[i - 1]);
			}
			result = scanner.nextInt();
		}

		return result;
	}
	
	public static Enum getMultipleChoiceResult(String question, Scanner scanner, Enum... answers) {
		int result = 0;

		while (result > answers.length | result < 1) {
			System.out.println(question);
			for (int i = 1; i <= answers.length; i++) {
				System.out.println("(" + i + ") " + answers[i - 1].toString());
			}
			result = scanner.nextInt();
		}

		return answers[result-1];
	}
}
