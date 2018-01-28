package de.maxkroner.model;

import de.maxkroner.values.Strings;
import sx.blah.discord.handle.obj.IChannel;

public class GuessThePicGame extends Game{

	public GuessThePicGame(IChannel channel) {
		super(channel);
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName() {
		return Strings.GUESS_THE_PIC_NAME;
	}
	




}
