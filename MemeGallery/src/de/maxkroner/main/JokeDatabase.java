package de.maxkroner.main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Random;

public abstract class JokeDatabase {
	private static final String DB_DRIVER = "org.h2.Driver";
	private static final String DB_CONNECTION = "jdbc:h2:~/jokes;MV_STORE=FALSE;MVCC=FALSE";
	private static final String DB_USER = "sa";
	private static final String DB_PASSWORD = "";

	private static Connection conn;

	public static void connect() {
		try {
			Class.forName(DB_DRIVER);
			conn = DriverManager.getConnection(DB_CONNECTION, DB_USER, DB_PASSWORD);
			System.out.println("Database connected.");
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
	}

	public static void disconnect() {
		try {
			conn.close();
			System.out.println("Database connected.");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void createTable() {
		try {
			Statement std = conn.createStatement();
			std.execute("DROP TABLE IF EXISTS joke");
			std.execute(
					"CREATE TABLE joke(id int auto_increment primary key, text varchar(255), category varchar(255))");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void insertJoke(String joke, String category) {
		try {
			Statement std = conn.createStatement();
			String query = "INSERT INTO joke (text, category) VALUES ('" + joke + "','" + category + "');";
			std.executeUpdate(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public static void insertJokes(List<String> jokes, String category) {
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

	public static void printAllJokes() {

		try {
			Statement std = conn.createStatement();
			String query = "SELECT * FROM joke";
			ResultSet rs = std.executeQuery(query);
			while (rs.next()) {
				System.out.println(rs.getString(2) + " - " + rs.getString(3));
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public static void deleteCategory(String category) {
		try {
			Statement std = conn.createStatement();
			String query = "DELETE FROM joke WHERE category = '" + category + "'";
			std.executeUpdate(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public static String getRandomJoke(String category) throws ClassNotFoundException, SQLException {
		String joke = "";
		Statement std = conn.createStatement();

		// get random joke index
		int jokecount = 1;
		String count_query = "SELECT COUNT(*) FROM joke WHERE category = '" + category + "'";
		ResultSet rs_count = std.executeQuery(count_query);
		while (rs_count.next()) {
			jokecount = rs_count.getInt(1);
		}
		Random r = new Random();
		int randomJokeNumber = r.nextInt(jokecount - 1) + 1;

		// get joke with specific index
		String select_query = "SELECT * FROM joke WHERE id = " + randomJokeNumber + " AND category = '" + category
				+ "'";
		ResultSet select_rs = std.executeQuery(select_query);
		while (select_rs.next()) {
			joke = select_rs.getString(2);
		}

		return joke;
	}
}
