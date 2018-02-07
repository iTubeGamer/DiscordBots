package de.maxkroner.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;



import org.pmw.tinylog.Logger;

import de.maxkroner.values.Values;

public class GTPDatabase extends Database {
	private static final String GTP_DATABASE_NAME = "gmgtp";

	public GTPDatabase() {
		this(GTP_DATABASE_NAME);
	}
	
	public GTPDatabase(String databaseName){
		super(databaseName);
		createTablesIfNotExist();
	}

	protected void createTablesIfNotExist() {
		executeStatement(Values.GTP_SQL_CREATE_LISTS);
		executeStatement(Values.GTP_SQL_CREATE_LIST_NESTING);
		executeStatement(Values.GTP_SQL_CREATE_WORDS);
		executeStatement(Values.GTP_SQL_CREATE_IMAGES);
		executeStatement(Values.GTP_SQL_NESTING_INTEGRITY_TRIGGER);
	}

	/**
	 * adds a word to the specified list in the database
	 * 
	 * @param guild_id
	 *            guild which owns this list, 0 if general list
	 * @param list_name
	 *            name of the list to which the word should be added
	 * @param word
	 *            the word to add
	 * @param urls
	 *            list of image_urls for the word
	 * @return returns the word_id generated when inserting the word
	 * @throws SQLException 
	 */
	public long addWordWithUrlsToList(long guild_id, String list_name, String word, List<String> urls) throws SQLException {
		long list_id = getListIdForList(guild_id, list_name);
		long word_id = addWordToList(word, list_id);
		
		String insert_query = "INSERT INTO images (url, word_id) VALUES(?, ?);";
		PreparedStatement st = conn.prepareStatement(insert_query);
		
		for (String url : urls) {
			st.setString(1, url);
			st.setLong(2, word_id);
			st.addBatch();
		}
		
		st.executeBatch();

		Logger.info("Inserted word {} with {} urls for list {} into database", word, urls.size(), list_id);
		return word_id;
	}

	/**
	 * insert word into database
	 * 
	 * @param word
	 * @param list_id
	 * @return the word_id generated when inserting
	 * @throws SQLException 
	 */
	private long addWordToList(String word, long list_id) throws SQLException {
		String query = "INSERT INTO words (name, list_id) VALUES (?, ?);";
		PreparedStatement st = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
		st.setString(1, word);
		st.setLong(2, list_id);
		int affectedRows = st.executeUpdate();
		
		if (affectedRows == 0) {
            throw new SQLException("Creating word failed, no rows affected.");
        }
		
		 try (ResultSet generatedKeys = st.getGeneratedKeys()) {
	            if (generatedKeys.next()) {
	                return generatedKeys.getLong(1);
	            }
	            else {
	                throw new SQLException("Creating word failed, no ID obtained.");
	            }
	        }
	}

	private long getListIdForList(long guild_id, String list_name) throws SQLException {
		ResultSet rs = getResultSetFromQuery("SELECT list_id FROM lists WHERE name='" + list_name + "' AND guild_id=" + guild_id + ";");
		rs.next();
		return rs.getLong(1);
	}
	
	/**
	 * Creates a new list in the database
	 * 
	 * @param list_name
	 * @param language
	 * @param description
	 * @param guild_id
	 * @return the list_id generated when creating the list
	 * @throws SQLException
	 */
	public long addList(String list_name, String language, String description, long guild_id) throws SQLException{
		String query = "INSERT INTO lists (name, language, description, guild_id) VALUES (?, ?, ?, ?)";
			PreparedStatement st = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			st.setString(1, list_name);
			st.setString(2, language);
			st.setString(3, description);
			st.setLong(4, guild_id);
			int affectedRows = st.executeUpdate();
			
			if (affectedRows == 0) {
	            throw new SQLException("Creating list failed, no rows affected.");
	        }
			
			 try (ResultSet generatedKeys = st.getGeneratedKeys()) {
		            if (generatedKeys.next()) {
		                return generatedKeys.getLong(1);
		            }
		            else {
		                throw new SQLException("Creating list failed, no ID obtained.");
		            }
		        }
	}
	
	public long addListNesting(long outer_list_id, long inner_list_id) throws SQLException{
		String query = "INSERT INTO listnesting (outer_list_id, inner_list_id) VALUES (?, ?)";
		
		PreparedStatement st = conn.prepareStatement(query);
		
		st.setLong(1, outer_list_id);
		st.setLong(2, inner_list_id);
		
		int affectedRows = st.executeUpdate();
		
		if (affectedRows == 0) {
            throw new SQLException("Creating list nesting failed, no rows affected.");
        }
		
		 try (ResultSet generatedKeys = st.getGeneratedKeys()) {
	            if (generatedKeys.next()) {
	                return generatedKeys.getLong(1);
	            }
	            else {
	                throw new SQLException("Creating nest listing failed, no ID obtained.");
	            }
	        }
			
	}

}
