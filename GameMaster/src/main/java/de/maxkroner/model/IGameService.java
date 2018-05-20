package de.maxkroner.model;

import de.maxkroner.db.GameMasterDatabase;
import de.maxkroner.implementation.IClientService;

public interface IGameService extends IClientService{

	public void gameStopped(IGame game);
	
	public GameMasterDatabase getDatabase();
}
