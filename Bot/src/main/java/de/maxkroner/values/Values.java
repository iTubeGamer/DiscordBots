package de.maxkroner.values;

import java.util.Arrays;
import java.util.List;

public class Values {
	
	///---SQL---///
		public static final String SQL_DROP_ALL_TABLES = "DROP ALL OBJECTS DELETE FILES";
		
		public static final String SQL_CREATE_TABLE_GUILDPROPERTIES_STRING = "CREATE TABLE IF NOT EXISTS guildpropertiesstring (" +
				"guild_id bigint," +
				"property varchar(255) NOT NULL," +
				"value varchar(255) NOT NULL," +
				"PRIMARY KEY (guild_id,property));";
		
		public static final String SQL_CREATE_TABLE_GUILDPROPERTIES_BOOLEAN = "CREATE TABLE IF NOT EXISTS guildpropertiesboolean (" +
				"guild_id bigint," +
				"property varchar(255) NOT NULL," +
				"value boolean NOT NULL," +
				"PRIMARY KEY (guild_id,property));";
		
		public static final String SQL_CREATE_TABLE_GUILDPROPERTIES_INT = "CREATE TABLE IF NOT EXISTS guildpropertiesint (" +
				"guild_id bigint," +
				"property varchar(255) NOT NULL," +
				"value int NOT NULL," +
				"PRIMARY KEY (guild_id,property));";
		
		public static final List<String> commandsThatCantBeDisabled = Arrays.asList("enable", "e", "activate", "disable", "d", "deactive");

}
