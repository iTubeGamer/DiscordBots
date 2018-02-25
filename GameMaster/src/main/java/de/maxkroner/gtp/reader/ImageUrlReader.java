package de.maxkroner.gtp.reader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.pmw.tinylog.Logger;

import de.maxkroner.gtp.values.Keys;
import de.maxkroner.values.Values;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ImageUrlReader {
	private static final String url_pattern = "https://www.googleapis.com/customsearch/v1?key=%s&cx=%s&q=%s&start=%d&searchType=image&imgSize=large&fileType=\"jpg png\"";
	private static final OkHttpClient client = new OkHttpClient();
	private static int start;
	/**
	 * Trys to get a list of image-urls for the specified search-term.
	 * @param term to use for the image search
	 * @param amount the amount of images to ask for
	 * @return list of Strings if successful or else an empty list
	 */
	public static List<String> getImageUrlsForTerm(String term, int amount) {
		
		try{	
			
			List<String> urls = new ArrayList<>();
			start = 1;
			
			while(urls.size() < amount){

				List<String> newUrls = getJSONResponseFor(term, start) //get HTTP Result as Optional<String>
								.flatMap(T -> Optional.of(T.optJSONArray("items"))).map(JSONArray::toList) //List of Objects in "items"
								.map(T -> T.stream().map(ImageUrlReader::getLinkFromItem) //try to retrieve an image-link from objects
													.map(ImageUrlReader::increaseStart)
													.filter(Optional::isPresent) //only continue with objects where image link was found
													.map(Optional::get)
													.filter(X -> X.matches(Values.URL_PATTERN))
													.collect(Collectors.toList()))					
								.orElse(Collections.emptyList());//return emptyList if it fails at some point
				
				if(newUrls.isEmpty()){
					return newUrls;
				}
				
				urls.addAll(newUrls);
								 
			}
			
			return urls;
		
		} catch (Exception e){
			Logger.error(e);
			return Collections.emptyList();
		}
			
	}
	
	private static <T> T increaseStart(T o){
		start++;
		return o;
	}

	private static Optional<JSONObject> getJSONResponseFor(String term, int start) {
		Boolean keyHasReachedItsLimit = true;
		JSONObject json = new JSONObject();
		
		while(keyHasReachedItsLimit){
			//get next key in queue
			String key;
			if(Keys.hasNext()){
				key = Keys.getKey();
			} else {
				return Optional.empty();
			}
			
			//build url with key
			String url = String.format(url_pattern, key, Keys.google_search_id, term, start);
			
			//try to make request
			Request request = new Request.Builder().url(url).addHeader("Referer", "maxkroner.ddnss.de").build();
			try (Response response = client.newCall(request).execute()) {
				
				//parse Response if successful
				String rspString = response.body().string();
				json = new JSONObject(rspString);
				
				//if key limit is reached try next key
				if(json.has("error")){
					if(key!=null){
						return Optional.empty();
						//Keys.removeKey(key);
					}	
				} else {
					keyHasReachedItsLimit = false;
				}	
			} catch (IOException e) {
				//return empty optional of parsing fail
				return Optional.empty();
			}
		}
		
		return Optional.of(json);
	}
	
	@SuppressWarnings("rawtypes")
	private static Optional<String> getLinkFromItem(Object item){
		try{
			return Optional.of((String) ((HashMap) item).get("link"));
		} catch (Exception e){
			Logger.error(e);
			return Optional.empty();
		}
	}

	public static void addImageUrlsToWordList(WordListTO wordList) {
		Set<String> words = wordList.getWordMap().keySet();
		for (String word : words) {
			List<String> urlsForWord = getImageUrlsForTerm(word, Values.IMAGES_PER_WORD_IN_DB);
			wordList.addUrlForWord(word, urlsForWord);
		}
		
	}
	
	
	

}
