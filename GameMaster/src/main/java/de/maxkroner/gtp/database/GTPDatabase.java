package de.maxkroner.gtp.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.pmw.tinylog.Logger;

import de.maxkroner.database.BotDatabase;
import de.maxkroner.db.GameMasterDatabase;
import de.maxkroner.gtp.reader.ImageUrlReader;
import de.maxkroner.gtp.reader.WordListReader;
import de.maxkroner.gtp.reader.WordListTO;
import de.maxkroner.values.Values;

public class GTPDatabase {
	private GameMasterDatabase db;
	private Connection conn;

	public GTPDatabase(GameMasterDatabase db) {
		this.db = db;
		this.conn = db.getConn();
		createTablesIfNotExist();
	}

	protected void createTablesIfNotExist() {
		db.executeStatement(Values.GTP_SQL_CREATE_LISTS);
		db.executeStatement(Values.GTP_SQL_CREATE_LIST_NESTING);
		db.executeStatement(Values.GTP_SQL_CREATE_WORDS);
		db.executeStatement(Values.GTP_SQL_CREATE_IMAGES);
		db.executeStatement(Values.GTP_SQL_NESTING_INTEGRITY_TRIGGER);
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
		long list_id = getListIdByNameAndGuild(guild_id, list_name);
		return addWordWithUrlsToList(list_id, word, urls);	
	}
	
	public long addWordWithUrlsToList(long list_id, String word, List<String> urls) throws SQLException {
		long word_id = addWordToList(word, list_id);
		
		String insert_query = "INSERT INTO images (url, word_id) VALUES(?, ?);";
		PreparedStatement st = conn.prepareStatement(insert_query);
		
		for (String url : urls) {
			st.setString(1, url);
			st.setLong(2, word_id);
			st.addBatch();
		}
		
		st.executeBatch();

		Logger.info("Inserted word {} with {} urls for list with id {} into database", word, urls.size(), list_id);
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
	
	public List<String> getAllListsByGuild(long guild_id){
		ArrayList<String> lists = new ArrayList<String>();
		String query = "SELECT name FROM lists WHERE guild_id=" + guild_id + " OR guild_id=0;";
		ResultSet rs = db.getResultSetFromQuery(query);
		try{
		while(rs.next()){
			lists.add(rs.getString(1));
		}
		} catch (SQLException e){
			Logger.error(e);
		}
		
		return lists;
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
	
	public long getOrCreateList(String list_name, String language, String description, long guild_id) throws SQLException{		
		Long list_id = getListIdByNameAndGuild(guild_id, list_name);
		if(list_id == null){
				return addList(list_name, language, description, guild_id);	
		} else {
			return list_id;
		}		
	}
	
	/**
	 * Pro
	 * 
	 * @param guild_id
	 * @param list_name
	 * @return
	 * @throws SQLException
	 */
	public Long getListIdByNameAndGuild(long guild_id, String list_name) throws SQLException {
		//does a guild specific list with the given name exist?
		ResultSet rs = db.getResultSetFromQuery("SELECT list_id FROM lists WHERE name='" + list_name + "' AND guild_id=" + guild_id + ";");
		if(rs.next()){
			return rs.getLong(1);
		} else {
			//does a general list with the given name exist?
			rs = db.getResultSetFromQuery("SELECT list_id FROM lists WHERE name='" + list_name + "' AND guild_id=0;");
			if(rs.next()){
				return rs.getLong(1);
			} else {
				return null;
			}		
		}
	}
	
	public List<String> getWordsForList(long list_id) throws SQLException{
		List<String> words = new ArrayList<>();
		ResultSet rs = db.getResultSetFromQuery("SELECT name FROM words WHERE list_id =" + list_id);
		while(rs.next()){
			words.add(rs.getString(1));
		}
		return words;
	}
	
	private Set<String> getRandomImagesForWord(long word_id, int amount) throws SQLException{
		Set<String> urls = new HashSet<>();
		ResultSet rs = db.getResultSetFromQuery("SELECT url FROM images WHERE word_id =" + word_id, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		if(rs.last() && (rs.getRow() >= amount)){
			int availabeImages = rs.getRow();
			while(urls.size() < amount){
				int index = ThreadLocalRandom.current().nextInt(1, availabeImages + 1);
				rs.absolute(index);
				urls.add(rs.getString(1));
			}
			return urls;
		}
		throw new SQLException("Error when trying to find random images.");
	}
	
	/**
	 * 
	 * @param list_id the id of the list to select a word from
	 * @param imageAmount how many imageUrls to get with the word
	 * @return word object
	 * @throws SQLException
	 */
	public Word getRandomWordFromList(long list_id, int imageAmount) throws SQLException{
		ResultSet rs = db.getResultSetFromQuery("SELECT word_id, name FROM words WHERE list_id =" + list_id, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		if(rs.last()){
				int index = ThreadLocalRandom.current().nextInt(1, rs.getRow() + 1);
				rs.absolute(index);
				long word_id = rs.getLong(1);
				String wordName = rs.getString(2);
				Word word = new Word(word_id, wordName);
				word.setImageUrls(getRandomImagesForWord(word_id, imageAmount));
				return word;
		} else {
			throw new SQLException("No words founds to pick from");
		}
		
	}
	
	public boolean existsWordInList(String word, long list_id) throws SQLException{
		String query = "SELECT COUNT(*) FROM words WHERE name='" + word + "' AND list_id=" + list_id;
		ResultSet rs = db.getResultSetFromQuery(query);
		if(rs.next()){
			int count = rs.getInt(1);
			if(count == 1){
				return true;
			} else if(count == 0){
				return false;
			}
		}
		
		throw new SQLException("Check if word exists in list has an error.");
	}
	
	public void updateWordDatabase(){
		List<WordListTO> wordListsWithoutUrls = WordListReader.getWordListsWithoutUrls();
		for (WordListTO wordListTO : wordListsWithoutUrls) {
			try {
				long list_id = getOrCreateList(wordListTO.getName(), wordListTO.getLanguage(), wordListTO.getDescription(), wordListTO.getGuild_id());
				removeWordsAlreadyInDatabase(wordListTO, list_id);
				ImageUrlReader.addImageUrlsToWordList(wordListTO);
				//insert Urls into DB
				for (Entry<String, List<String>> wordSet : wordListTO.getWordMap().entrySet()){
					addWordWithUrlsToList(list_id, wordSet.getKey(), wordSet.getValue());
				}	
			} catch (SQLException e) {
				Logger.error("SQL Error when trying to update wordlist {} in database on startup.", wordListTO.getName());
			}
		}
	}

	private void removeWordsAlreadyInDatabase(WordListTO wordListTO, long list_id) throws SQLException {
		Set<String> wordsInList = wordListTO.getWordMap().keySet();
		Set<String> wordsToRemove = new HashSet<>();
		for (String word : wordsInList) {
			if(existsWordInList(word, list_id)){
				wordsToRemove.add(word);
			}
		}
		for (String word : wordsToRemove) {
			wordListTO.removeWord(word);
		}
	}

}
