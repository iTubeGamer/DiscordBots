package de.maxkroner.implementation;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.maxkroner.factory.GameProducer;
import de.maxkroner.model.GameService;
import de.maxkroner.model.GameState;
import de.maxkroner.model.IGame;
import de.maxkroner.parsing.Command;
import de.maxkroner.parsing.CommandHandler;
import de.maxkroner.values.Values;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;

public class GameMasterBot extends Bot implements GameService {
	private GameProducer producer;
	private Map<IChannel, IGame> gameList = new HashMap<>();

	public GameMasterBot() {
		super("GameMaster");
		addCommandParsing(this.getClass(), Values.PREFIX, Values.OPTION_PREFIX, true);
	}

	public void setGameProducer(GameProducer producer) {
		this.producer = producer;
	}

	@Override
	public void disconnect() {

	}

	@CommandHandler(Values.COMMAND_CREATE)
	protected void createGame(MessageReceivedEvent event, Command command) {
		if ((command.getArguments().orElse(Collections.emptyList()).size() >= 1) && !command.hasOptions()) {
			if (gameList.containsKey(event.getChannel())) {
				event.getAuthor().getOrCreatePMChannel()
						.sendMessage(Values.getMessageString(Values.MESSAGE_CREATE_GAME_ALREADY_EXISTS, event.getChannel().getName()));
				return;
			}
			
			List<String> args = command.getArguments().get();

			producer.createGame(this, args, event).ifPresent(T -> {
				gameList.put(event.getChannel(), T);
				sendMessage(Values.getMessageString(Values.MESSAGE_CREATE_GAME_SUCCESS, gameList.get(event.getChannel()).getName()),
						event.getChannel(), false);
			});

		}
	}

	@CommandHandler(Values.COMMAND_JOIN)
	protected void joinGame(MessageReceivedEvent event, Command command) {
		if (!command.hasOptionsOrArguments() && gameList.containsKey(event.getChannel())) {

			IGame game = gameList.get(event.getChannel());
			if (!game.getPlayers().contains(event.getAuthor())) {
				game.addPlayer(event.getAuthor());
				sendMessage(Values.getMessageString(Values.MESSAGE_PLAYER_JOINED, event.getAuthor().toString()), event.getChannel(), false);
			} else {
				sendMessage(Values.getMessageString(Values.MESSAGE_ALREADY_ON_PLAYERLIST, event.getAuthor().toString()), event.getChannel(), false);
			}

		}
	}

	@CommandHandler(Values.COMMAND_START)
	protected void startGame(MessageReceivedEvent event, Command command) {
		if (!command.hasOptionsOrArguments() && gameList.containsKey(event.getChannel())) {

			IGame game = gameList.get(event.getChannel());

			if (game.getGameState() == GameState.GameSetup) {
				game.start();
			}
		}

	}

	@CommandHandler(Values.COMMAND_SCOREBOARD)
	protected void scoreBoard(MessageReceivedEvent event, Command command) {
		if (!command.hasOptionsOrArguments() && gameList.containsKey(event.getChannel())) {
			IGame game = gameList.get(event.getChannel());
			game.printScoreBoard();
		}
	}

	@CommandHandler(Values.COMMAND_GTP_NEXT_ROUND)
	protected void nextRound(MessageReceivedEvent event, Command command) {
		if (!command.hasOptionsOrArguments() && gameList.containsKey(event.getChannel())) {
			IGame game = gameList.get(event.getChannel());
			if (game.getGameOwner().equals(event.getAuthor())) {
				game.nextRound();
			}
		}
	}

	@CommandHandler(Values.COMMAND_STOP)
	protected void stopGame(MessageReceivedEvent event, Command command) {
		if (!command.hasOptionsOrArguments() && gameList.containsKey(event.getChannel())) {

			IGame game = gameList.get(event.getChannel());

			if (game.getGameOwner().equals(event.getAuthor())) {
				game.stop();
				gameStopped(game);
			}

		}
	}

	@EventSubscriber
	public void onMessageReceivedEvent(MessageReceivedEvent event) {
		super.onMessageReceivedEvent(event);

		if (gameList.containsKey(event.getChannel())) {
			IGame game = gameList.get(event.getChannel());
			if (game.getPlayers().contains(event.getAuthor())) {
				game.onMessageReceivedEvent(event);
			}
		}
	}

	@Override
	public void sendMessage(String message, IChannel channel, boolean tts) {
		super.sendMessage(message, channel, tts);
	}

	@Override
	public void gameStopped(IGame game) {
		sendMessage(Values.getMessageString(Values.MESSAGE_GAME_OVER, game.getName()), game.getChannel(), false);
		gameList.remove(game.getChannel());
	}

}
