package de.maxkroner.implementation;

import java.util.List;

import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.obj.IExtendedInvite;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

public class InfoBot extends Bot {

	public InfoBot() {
		super("InfoBot");

	}
	
	@Override
	@EventSubscriber
	public void onReady(ReadyEvent event) {
		super.onReady(event);
		
		printConnectedGuilds();
		
		printUsers();
		
		printInvites();

	}


	private void printConnectedGuilds() {
		
		List<IGuild> guilds = getClient().getGuilds();
		
		for (IGuild guild : guilds) {
			System.out.println(guild.getName());
		}
	}


	private void printInvites() {
		for (IGuild guild : getClient().getGuilds()) {
			List<IExtendedInvite> extendedInvites = guild.getExtendedInvites();
			
			for (IExtendedInvite extendedInvite : extendedInvites) {
				System.out.println(extendedInvite.getCode());
			}
		}
	}
	
	private void printUsers(){
		for (IGuild guild : getClient().getGuilds()) {
			List<IUser> users = guild.getUsers();
			System.out.println("Connected Users for guild " + guild.getName() + ":");
			for (IUser user : users) {
				System.out.println(user.getName() + "#" + user.getDiscriminator());
			}
		}	
	}

	@Override
	public void disconnect() {
		
	}




}
