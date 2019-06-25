package tokenfinder;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ScryfallDataManager {
	public static List<Card> loadCards() throws Exception {
		try {
            InputStream in = CardController.class.getResourceAsStream("/scryfall-clean.json");
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
            InputStream in = CardController.class.getResourceAsStream("/scryfall-tokens.json");
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
}
