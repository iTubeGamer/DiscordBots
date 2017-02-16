package de.maxkroner;



public class Main {

	public static void main(String[] args) {
		if (args.length < 1) // Needs a bot token provided
			throw new IllegalArgumentException("Please provide the Bot-Token as argument!");
		
		BaseBot bot = new BaseBot(args[0]);
	}
	
	 

}
