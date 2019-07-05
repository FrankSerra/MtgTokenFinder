package tokenfinder;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ScryfallDataManager {
	public static List<Card> loadCards() throws Exception {
		try {
            InputStream in = CardController.class.getResourceAsStream("/static/data/scryfall-clean.json");
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            Type collectionType = new TypeToken<List<Card>>(){}.getType();
            
            @SuppressWarnings("unchecked")
			List<Card> cards = (List<Card>) new Gson().fromJson(br, collectionType);
            
            return cards;
		}
		catch(Exception e) {
            throw new Exception("Cannot load card information from file.");
        }
	}
    
    public static List<Card> loadTokens() throws Exception {
		try {
            InputStream in = CardController.class.getResourceAsStream("/static/data/scryfall-tokens.json");
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            Type collectionType = new TypeToken<List<Card>>(){}.getType();
            
            @SuppressWarnings("unchecked")
			List<Card> cards = (List<Card>) new Gson().fromJson(br, collectionType);
            
            return cards;
		}
		catch(Exception e) {
            throw new Exception("Cannot load token information from file.");
        }
	}
    
    public static ArrayList<Card> loadTipCards() throws Exception {
		try {
            InputStream in = CardController.class.getResourceAsStream("/static/data/scryfall-tip-cards.json");
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            Type collectionType = new TypeToken<ArrayList<Card>>(){}.getType();
            
            @SuppressWarnings("unchecked")
			ArrayList<Card> cards = (ArrayList<Card>) new Gson().fromJson(br, collectionType);
            
            return cards;
		}
		catch(Exception e) {
            throw new Exception("Cannot load tip card information from file.");
        }
	}
    
    public enum ImageSize {
    	small, normal
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
