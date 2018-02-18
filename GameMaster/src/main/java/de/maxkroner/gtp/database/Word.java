package de.maxkroner.gtp.database;

import java.util.Set;

public class Word {
	private long word_id;
	private String word;
	private Set<String> imageUrls;

	public Word(long word_id, String word) {
		super();
		this.word_id = word_id;
		this.word = word;
	}

	public long getWord_id() {
		return word_id;
	}
	public String getWord() {
		return word;
	}
	public Set<String> getImageUrls() {
		return imageUrls;
	}
	public String getAndRemoveUrl() {
		String url = imageUrls.iterator().next();
		imageUrls.remove(url);
		return url;
	}
	public void setImageUrls(Set<String> imageUrls) {
		this.imageUrls = imageUrls;
	}
	

}
