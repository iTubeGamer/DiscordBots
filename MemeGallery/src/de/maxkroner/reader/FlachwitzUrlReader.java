package de.maxkroner.reader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class FlachwitzUrlReader {
	private static final String url = "https://1a-flachwitze.de/neueste/page/";
	private int pageCount;
	private int jokeCount;

	public FlachwitzUrlReader() {
		pageCount = 1;
		jokeCount = 0;
	}

	public FlachwitzUrlReader(int startpage) {
		this();
		pageCount = startpage;
	}

	public String nextJoke() {
		String joke = "";

		try {
			Document doc;
			doc = Jsoup.connect(url + pageCount).get();
			Elements jokes = doc.getElementsByClass("status-publish");
			Element jokeElement = jokes.get(jokeCount);

			String question = jokeElement.getElementsByTag("a").get(0).text();
			String answer = jokeElement.getElementsByTag("p").get(0).text();
			joke = question + " - " + answer;

			jokeCount++;
			if (jokeCount == 4) {
				jokeCount = 0;
				pageCount++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return joke;
	}

	public List<String> getJokes(int count) {
		List<String> jokes = new ArrayList<>();
		// switch to new page to start
		if (jokeCount != 0) {
			jokeCount = 0;
			pageCount++;
		}

		for (int i = 0; i < count; i = i + 4) {

			try {
				Document doc;
				doc = Jsoup.connect(url + pageCount).get();
				Elements jokesElements = doc.getElementsByClass("status-publish");
				pageCount++;
				for (Element jokeElement : jokesElements) {
					String question = jokeElement.getElementsByTag("a").get(0).text();
					String answer = jokeElement.getElementsByTag("p").get(0).text();
					String joke = question + " - " + answer;
					jokes.add(joke);
				}

				System.out.println((i + 4) + "/" + count);
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		return jokes;
	}

}
