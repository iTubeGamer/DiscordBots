package de.maxkroner.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

import org.pmw.tinylog.Logger;

import de.maxkroner.values.Values;

public class BotDatabase {
	private static final String DB_DRIVER = "org.h2.Driver";
	private static String DB_CONNECTION;
	private static final String DB_USER = "sa";
	private static final String DB_PASSWORD = "";

	protected Connection conn;
	
	public BotDatabase(String pathName){
		try {
			DB_CONNECTION = "jdbc:h2:" + pathName + ";MV_STORE=FALSE;MVCC=FALSE";
			Class.forName(DB_DRIVER);
			conn = DriverManager.getConnection(DB_CONNECTION, DB_USER, DB_PASSWORD);
			createTablesIfNotExist();
		} catch (Exception e) {
			Logger.error("Database connection failed.");
			e.printStackTrace();
		}		
	}
	
	public void resetDatabase()
	{
		executeStatement(Values.SQL_DROP_ALL_TABLES);
		createTablesIfNotExist();
	}
	
	public void addGuildProperty(long guild_id, String property, String value) throws SQLException{
		String query = "MERGE INTO guildpropertiesstring (guild_id, property, value) VALUES (?, ?, ?)";
		PreparedStatement st = conn.prepareStatement(query);
		st.setLong(1, guild_id);
		st.setString(2, property);
		st.setString(3, value);
		
		int affectedRows = st.executeUpdate();
		
		if (affectedRows == 0) {
            throw new SQLException("Inserting property failed, no rows affected.");
        }
	}
	
	public Optional<String> getStringGuildProperty(long guild_id, String property) throws SQLException{
		String query = "SELECT value FROM guildpropertiesstring WHERE guild_id=" + guild_id
														+ "AND property='" + property + "';";
		
		ResultSet rs = getResultSetFromQuery(query);
		if(rs.next()){
			return Optional.of(rs.getString(1));
		} else {
			return Optional.empty();
		}
	}
	
	public void addGuildProperty(long guild_id, String property, boolean value) throws SQLException{
		String query = "MERGE INTO guildpropertiesboolean (guild_id, property, value) VALUES (?, ?, ?)";
		PreparedStatement st = conn.prepareStatement(query);
		st.setLong(1, guild_id);
		st.setString(2, property);
		st.setBoolean(3, value);
		
		int affectedRows = st.executeUpdate();
		
		if (affectedRows == 0) {
            throw new SQLException("Inserting property failed, no rows affected.");
        }
	}
	
	public Optional<Boolean> getBooleanGuildProperty(long guild_id, String property) throws SQLException{
		String query = "SELECT value FROM guildpropertiesboolean WHERE guild_id=" + guild_id
														+ "AND property='" + property + "';";
		
		ResultSet rs = getResultSetFromQuery(query);
		if(rs.next()){
			return Optional.of(rs.getBoolean(1));
		} else {
			return Optional.empty();
		}
	}
	
	public void addGuildProperty(long guild_id, String property, int value) throws SQLException{
		String query = "MERGE INTO guildpropertiesint (guild_id, property, value) VALUES (?, ?, ?)";
		PreparedStatement st = conn.prepareStatement(query);
		st.setLong(1, guild_id);
		st.setString(2, property);
		st.setInt(3, value);
		
		int affectedRows = st.executeUpdate();
		
		if (affectedRows == 0) {
            throw new SQLException("Inserting property failed, no rows affected.");
        }
	}
	
	public Optional<Integer> getIntGuildProperty(long guild_id, String property) throws SQLException{
		String query = "SELECT value FROM guildpropertiesint WHERE guild_id=" + guild_id
														+ "AND property='" + property + "';";
		
		ResultSet rs = getResultSetFromQuery(query);
		if(rs.next()){
			return Optional.of(rs.getInt(1));
		} else {
			return Optional.empty();
		}
	}
	
	protected void createTablesIfNotExist(){
		executeStatement(Values.SQL_CREATE_TABLE_GUILDPROPERTIES_STRING);
		executeStatement(Values.SQL_CREATE_TABLE_GUILDPROPERTIES_BOOLEAN);
		executeStatement(Values.SQL_CREATE_TABLE_GUILDPROPERTIES_INT);
	}
	
	protected void close() {
		try {
			conn.close();
			Logger.info("Database disconnected.");
		} catch (SQLException e) {
			Logger.error(e);
		}
	}

	protected Integer executeStatement(String query) {
		try {
			Statement std = conn.createStatement();
			return std.executeUpdate(query);
		} catch (SQLException e) {
			Logger.error(e);
			return null;
		}	
	}
	

	
	public ResultSet getResultSetFromQuery(String query) {
		Statement std;
		try {
			std = conn.createStatement();
			return std.executeQuery(query);
		} catch (SQLException e) {
			Logger.error(e);
			return null;
		}
	}
	
	public int getRowCount(String query) throws SQLException{
		ResultSet rs = getResultSetFromQuery(query);
		if(rs.next()){
			return rs.getInt(1);
		} else {
			throw new SQLException("Error in SQL Count query.");
		}
	}

	
}

