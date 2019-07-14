package ScryfallData;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import HelperObjects.ImageSize;
import tokenfinder.CardController;

public class ScryfallDataManager {
	public ArrayList<Card> cards, tokens, tipcards;
	
	public ScryfallDataManager(boolean includeSilver) {
		try {
			
			this.cards = loadCards();
			if(includeSilver)
				this.cards.addAll(loadSilverCards());
			
			this.tokens = loadTokens();
			this.tipcards = loadTipCards();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	private static ArrayList<Card> loadResource(String file, String exceptionMsg) throws Exception {
		try {
            InputStream in = CardController.class.getResourceAsStream(file);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            Type collectionType = new TypeToken<List<Card>>(){}.getType();
            
            Gson gson = new Gson();
            Object response = gson.fromJson(br, collectionType);
            ArrayList<Card> cards = (ArrayList<Card>) response;
            
            return cards;
		}
		catch(Exception e) {
            throw new Exception(exceptionMsg);
        }
	}
	
	private static ArrayList<Card> loadCards() throws Exception {
		return loadResource("/static/data/scryfall-clean.json", "Cannot load card information from file.");
	}
	
	private static ArrayList<Card> loadSilverCards() throws Exception {
		return loadResource("/static/data/scryfall-silver.json", "Cannot load silver card information from file.");
	}
    
	private static ArrayList<Card> loadTokens() throws Exception {
		return loadResource("/static/data/scryfall-tokens.json", "Cannot load token information from file.");
	}
    
	private static ArrayList<Card> loadTipCards() throws Exception {
		return loadResource("/static/data/scryfall-tip-cards.json", "Cannot load tip card information from file.");
	}
    
    public static String googleformURL() {
    	return "https://forms.gle/vpwhqshxLTTV8eVa9";
    }
    
    public static String getImageApiURL(Card c, ImageSize size, boolean back) {
    	String root = "https://api.scryfall.com/cards/" + c.set + "/" + c.collector_number + "?format=image&version=";
    	
    	switch(size) {
	    	case small:
	    		root += "small";
	    		break;
	    		
	    	case normal:
			default:
	    		root += "normal";
	    		break;
    	}
    	
    	if(back)
    		root += "&face=back";
    	
    	return root;
    }
}
