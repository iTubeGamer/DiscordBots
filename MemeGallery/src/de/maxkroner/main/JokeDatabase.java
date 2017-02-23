package de.maxkroner.main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

import de.maxkroner.implementation.Joke;

public abstract class JokeDatabase {
	 private static final String DB_DRIVER = "org.h2.Driver";
	 private static final String DB_CONNECTION = "jdbc:h2:~/test1;MV_STORE=FALSE;MVCC=FALSE";
	 private static final String DB_USER = "sa";
	 private static final String DB_PASSWORD = "";
	
	
	public static Joke getRandomJoke() throws ClassNotFoundException, SQLException{
		Joke joke;
		String question = "";
		String answer = "";
		
		Connection conn;
		Class.forName(DB_DRIVER);
		conn = DriverManager.getConnection(DB_CONNECTION, DB_USER, DB_PASSWORD);
		Statement std = conn.createStatement();
		
		//get random joke index
		int jokecount = 1;
		String count_query = "SELECT COUNT(*) FROM joke";
		ResultSet rs_count = std.executeQuery(count_query);
		while (rs_count.next()){
			jokecount = rs_count.getInt(1);
		}
		Random r = new Random();
		int randomJokeNumber = r.nextInt(jokecount - 1) + 1;
		
		
		//get joke with specific index
		String select_query = "SELECT * FROM joke WHERE id = " + randomJokeNumber;
		ResultSet select_rs = std.executeQuery(select_query);
		while (select_rs.next()){
			question = select_rs.getString(2);
			answer = select_rs.getString(3);
		}
		
	    conn.close();
	    
	    joke = new Joke(question, answer);
	    return joke;
	}
}
