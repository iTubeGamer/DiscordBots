package de.maxkroner.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.pmw.tinylog.Logger;

import de.maxkroner.values.Values;

public abstract class Database {
	private static final String DB_DRIVER = "org.h2.Driver";
	private static String DB_CONNECTION;
	private static final String DB_USER = "sa";
	private static final String DB_PASSWORD = "";

	protected Connection conn;
	
	public Database(String name){
		try {
			DB_CONNECTION = "jdbc:h2:~/" + name + ";MV_STORE=FALSE;MVCC=FALSE";
			Class.forName(DB_DRIVER);
			conn = DriverManager.getConnection(DB_CONNECTION, DB_USER, DB_PASSWORD);
		} catch (Exception e) {
			Logger.error("Database connection failed.");
			e.printStackTrace();
		}		
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
	
	public void dropAllTables()
	{
		executeStatement(Values.SQL_DROP_ALL_TABLES);
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
