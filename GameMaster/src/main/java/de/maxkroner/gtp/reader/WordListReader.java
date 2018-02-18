package de.maxkroner.gtp.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.pmw.tinylog.Logger;

public class WordListReader {
	private static final String FOLDER = "gtplists";

	public static List<WordListTO> getWordListsWithoutUrls() {
		ArrayList<WordListTO> wordLists = new ArrayList<WordListTO>();
		File[] files = getResourceFolderFiles(FOLDER);
		
		for (File file : files) {
			try {
				WordListTO wordListTO = new WordListTO(file.getName(), 0);
				InputStream is = new FileInputStream(file);
				String line = "";
				BufferedReader reader = new BufferedReader(new InputStreamReader(is));
				if (is != null) {
					//first line: language
					if((line = reader.readLine()) != null){
						wordListTO.setLanguage(line);
					} else {
						throw new IOException("Wrong word list format.");
					}
					//second line: description
					if((line = reader.readLine()) != null){
						wordListTO.setDescription(line);
					} else {
						throw new IOException("Wrong word list format.");
					}
					
					while ((line = reader.readLine()) != null) {
						wordListTO.addWord(line);
					}
					
					if(wordListTO.getWordMap().isEmpty()){
							throw new IOException("No words found in list.");
					}
					
					wordLists.add(wordListTO);
				}
			} catch (IOException e) {
				Logger.error(e);
			}
		}

		return wordLists;
	}

	private static File[] getResourceFolderFiles(String folder) {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		URL url = loader.getResource(folder);
		String path = url.getPath();
		return new File(path).listFiles();
	}

}
