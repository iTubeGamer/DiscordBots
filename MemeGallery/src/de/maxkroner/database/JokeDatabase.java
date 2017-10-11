package de.maxkroner.database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class JokeDatabase {
	private static final String DB_DRIVER = "org.h2.Driver";
	private static final String DB_CONNECTION = "jdbc:h2:~/jokes;MV_STORE=FALSE;MVCC=FALSE";
	private static final String DB_USER = "sa";
	private static final String DB_PASSWORD = "";

	private Connection conn;
	
	public JokeDatabase(){
		try {
			Class.forName(DB_DRIVER);
			conn = DriverManager.getConnection(DB_CONNECTION, DB_USER, DB_PASSWORD);
			createTableIfNotExists();
			System.out.println("Database connected.");
		} catch (SQLException | ClassNotFoundException e) {
			System.out.println("Database connection failed.");
			e.printStackTrace();
		}		
	}

	private void createTableIfNotExists() {
		try {
			Statement std = conn.createStatement();
			std.execute(
					"CREATE TABLE IF NOT EXISTS joke(id int auto_increment primary key, text varchar(255), category varchar(255))");
		} catch (SQLException e) {
			e.printStackTrace();
		}	
	}

	public void close() {
		try {
			conn.close();
			System.out.println("Database disconnected.");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void createTable() {
		try {
			Statement std = conn.createStatement();
			std.execute("DROP TABLE IF EXISTS joke");
			std.execute(
					"CREATE TABLE joke(id int auto_increment primary key, text varchar(255), category varchar(255))");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public List<String> getJokeCategories() {
		List<String> categories = new ArrayList<>();

		Statement std;
		try {
			std = conn.createStatement();
			String query = "SELECT DISTINCT category FROM joke";
			ResultSet rs = std.executeQuery(query);
			while (rs.next()) {
				categories.add(rs.getString(1));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return categories;
	}

	public void insertJoke(String joke, String category) {
		try {
			Statement std = conn.createStatement();
			String query = "INSERT INTO joke (text, category) VALUES ('" + joke + "','" + category + "');";
			std.executeUpdate(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public void insertJokes(List<String> jokes, String category) {
		try {
			String insertString = "INSERT INTO joke (text, category) VALUES ( ? , ? );";
			PreparedStatement insertJokes = conn.prepareStatement(insertString);

			for (String joke : jokes) {
				insertJokes.setString(1, joke);
				insertJokes.setString(2, category);
				insertJokes.executeUpdate();
			}
			
			
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	private void printAllJokes(Optional<String> category) {
		String whereClause = "";
		if (category.isPresent()) {
			whereClause = " WHERE category = '" + category.get() + "'";
		}

		try {
			Statement std = conn.createStatement();
			String query = "SELECT * FROM joke" + whereClause;
			ResultSet rs = std.executeQuery(query);
			if(!rs.next()){
				System.out.println("There are no jokes to print...");
			}
			while (rs.next()) {
				System.out.println(rs.getString(2));
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void printAllJokes(String category) {
		Optional<String> optCategory = Optional.of(category);
		printAllJokes(optCategory);
	}

	public void printAllJokes() {
		Optional<String> opt = Optional.empty();
		printAllJokes(opt);
	}

	public void deleteCategory(String category) {
		try {
			Statement std = conn.createStatement();
			String query = "DELETE FROM joke WHERE category = '" + category + "'";
			std.executeUpdate(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public String getRandomJoke(Optional<String> category) {
		String whereClause = "";
		if (category.isPresent()) {
			whereClause = " WHERE category = '" + category.get() + "'";
		}
		
		String joke = "";
		Statement std;

		try {
			std = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

			// get all jokes of this category
			String select_query = "SELECT * FROM joke" + whereClause;
			ResultSet select_rs = std.executeQuery(select_query);
			
			if(select_rs.last()){
				// get random joke index
				int jokecount = select_rs.getRow();
				Random r = new Random();
				int randomJokeNumber = r.nextInt(jokecount - 1) + 1;
				//get joke at this index
				select_rs.absolute(randomJokeNumber);
				joke = select_rs.getString(2);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return joke;
	}
}
