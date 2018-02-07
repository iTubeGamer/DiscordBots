package de.maxkroner.implementation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;

import org.pmw.tinylog.Logger;

import de.maxkroner.parsing.CommandSet;
import de.maxkroner.parsing.MessageParsing;
import de.maxkroner.ui.BotMenue;
import de.maxkroner.parsing.Command;
import de.maxkroner.parsing.CommandHandler;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.shard.DisconnectedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.Image;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.RequestBuffer;

public abstract class Bot {
	private String bot_name = "";
	protected BotMenue botMenue;
	private IDiscordClient client;
	private CommandSet commandSet = null;
	private HashMap<String, HashSet<Method>> commandMethodsMap;
	
	public IDiscordClient getClient() {
		return this.client;
	}

	public Bot(String token, BotMenue botMenue) {
		Logger.info("|||---STARTING UP ---|||");
		this.client = createClient(token);
		this.botMenue = botMenue;
		EventDispatcher dispatcher = client.getDispatcher();
		dispatcher.registerListener(this); 
	}

	
	@EventSubscriber
	public void onReady(ReadyEvent event) {
		Bot bot = this;
		bot_name = client.getOurUser().getName();
		
		new Thread() {
			@Override
			public void run() {
				botMenue.startMenue(bot);
			}
		}.start();
		System.out.println("Logged in as " + bot_name);
		Logger.info("Logged in as " + bot_name);
	}
	
	@EventSubscriber
	protected void logout(DisconnectedEvent event) {
		System.out.println("Logged out for reason " + event.getReason() + "!");
		Logger.info("Logged out for reason " + event.getReason() + "!");
	}
	
	@EventSubscriber
	public void onMessageReceivedEvent(MessageReceivedEvent event) {
		if(commandSet != null){
			Command command = MessageParsing.parseMessageWithCommandSet(event.getMessage().getContent(), commandSet);
			if(command != null){
				notifyReceivers(event, command);
			}
		}
	}

	public void changeName(String name){
		try {
			client.changeUsername(name);
			System.out.println("The botname was changed to \"" + name + "\"");
			Logger.info("The botname was changed to \"" + name + "\"");
		} catch (Exception e) {
			System.out.println("Changing botname failed.");
			Logger.error(e);
		}
	}
	
	public void changePlayingText(String playingText){
		try {
			client.changePlayingText(playingText);
			System.out.println("The playingText was changed to \"" + playingText + "\"");
			Logger.info("The playingText was changed to \"" + playingText + "\"");
		} catch (Exception e) {
			System.out.println("Changing playingText failed.");
			Logger.error(e);
		}
	}
	
	public void changeAvatar(String url, String imageType){
		try {
			client.changeAvatar(Image.forUrl(imageType, url));
			System.out.println("The avatar has been successfully changed.");
			Logger.info("The avatar has been successfully changed.");
		} catch (Exception e) {
			System.out.println("Chaning avatar failed.");
			Logger.error(e);
		}
	}
	
	public abstract void disconnect();
	
	protected void sendMessage(String message, IChannel channel, Boolean tts) {
		RequestBuffer.request(() -> {
			try{
				MessageBuilder mb = new MessageBuilder(this.client).withChannel(channel);
				if (tts)
					mb.withTTS();
				mb.withContent(message);
				mb.build();
			} catch (DiscordException e){
				Logger.warn(e);
				throw e;
			}
		});
		
	}
	
	protected void sendPrivateMessage(IUser recepient, String message) {
		MessageBuilder mb = new MessageBuilder(this.client).withChannel(recepient.getOrCreatePMChannel());
		mb.withContent(message);
		mb.build();
	}

	public static IDiscordClient createClient(String token) {
		ClientBuilder clientBuilder = new ClientBuilder();
		clientBuilder.withToken(token);
		try {
			return clientBuilder.login();
											
		} catch (DiscordException e) {
			Logger.error(e);
			return null;
		}
	}
	
	//---Message Parsing Event System---//
	protected void addCommandParsing(Class<? extends Bot> botClass, String commandIdentifier, char optionIdentifier){
		createCommandMethodsMap(botClass);
		this.commandSet = new CommandSet(commandIdentifier, commandMethodsMap.keySet(), optionIdentifier);
	}
	
	protected void addCommandParsing(Class<? extends Bot> botClass){
		addCommandParsing(botClass, "!", '-');
	}

	private void createCommandMethodsMap(Class<? extends Bot> botClass) {
		commandMethodsMap = new HashMap<>();
		Method[] methods = botClass.getDeclaredMethods();
		for(final Method method: methods){
			if(method.isAnnotationPresent(CommandHandler.class)){
				CommandHandler annotation = method.getAnnotation(CommandHandler.class);
				String[] commands = annotation.value();
				if(commands.length >= 1){
					for(String command : commands){
						addMethodForCommandToMap(command, method);
					}
				}
			}
		}
	}

	private void addMethodForCommandToMap(String command, Method method) {
		if(!commandMethodsMap.containsKey(command)){
			commandMethodsMap.put(command, new HashSet<Method>());
		}		
		commandMethodsMap.get(command).add(method);	
	}
	
	private void notifyReceivers(MessageReceivedEvent event, Command command) {
		if (commandMethodsMap.containsKey(command.getName())){
			for(Method method: commandMethodsMap.get(command.getName())){
				try {
					method.invoke(this, event, command);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		}
		
		
	}

}