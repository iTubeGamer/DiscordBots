package de.maxkroner.gtp.reader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WordListTO {
	private String name;
	private String language;
	private String description;
	private long guild_id;
	private List<String> nestedLists = new ArrayList<>();
	private Map<String, List<String>> wordMap = new HashMap<>();

	public WordListTO(String name, long guild_id) {
		super();
		this.name = name;
		this.guild_id = guild_id;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public long getGuild_id() {
		return guild_id;
	}
	public void setGuild_id(long guild_id) {
		this.guild_id = guild_id;
	}
	
	public void addNestedList(String name){
		nestedLists.add(name);
	}
	
	public List<String> getNestedLists(){
		return nestedLists;
	}
	
	public void addWord(String name){
		wordMap.put(name, new ArrayList<>());
	}
	
	public void removeWord(String name){
		wordMap.remove(name);
	}
	
	public void addUrlForWord(String word, List<String> urlsForWord) {
		wordMap.put(word, urlsForWord);
	}

	public Map<String, List<String>> getWordMap(){
		return wordMap;
	}
	
	public List<String> getUrlsForWord(String word){
		return wordMap.get(word);
	}

	
}
