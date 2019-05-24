package tokenfinder;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@Controller
public class CardController {

    @GetMapping({"/", "/tokens"})
    public String cards(Model model) {
        return "cards";
    }
    
    @PostMapping("/tokens")
    public String tokens(@RequestParam(name="cardlist", required=true, defaultValue="") String cardlist, Model model) {
    	SearchResult sr = tokenResults(cardlist);
    	model.addAttribute("cardlist", sr.errors);
    	model.addAttribute("results", sr.tokenResults);
    	model.addAttribute("contains_create", sr.containsCreate);
    	
    	return "tokens";
    }
    
    public SearchResult tokenResults(String cardlist) {
    	List<String> errors = new ArrayList<String>();
    	List<TokenResult> results = new ArrayList<TokenResult>();
    	List<Card> containsCreate = new ArrayList<Card>();
    	
    	try { 		
	    	List<Card> cards = loadCards();
	    	List<Card> tokens = loadTokens();
	    	
	    	//Search terms
			String[] terms = cardlist.split("\\n");
			for (String term: terms) {
				term = term.replace("\r", "").replaceAll("[0-9]+\\w* ", "").trim();
				if(term.isEmpty() || StringUtils.containsIgnoreCase(term, "sideboard"))
					continue;
				
				Card found = findCardByName(cards, term);
				if(found != null && found.all_parts != null) {			
					boolean goteem = false;
					for (Iterator<Related_Card> r = found.all_parts.iterator(); r.hasNext();) {
						Related_Card rc = r.next();
						Card token = findToken(tokens, rc.id);
						if(token != null) {
							goteem = true;
							results = addTokenAndSources(results, token, found);
						}
					}
					if(!goteem && StringUtils.containsIgnoreCase(found.oracle_text, " create")) {
						containsCreate.add(found);
					}
				}
				else if(found != null && StringUtils.containsIgnoreCase(found.oracle_text, " create")) {
					containsCreate.add(found);
				}
				else if(found == null){
					errors.add(term + " not found.");
				}
			}
			
			errors.removeIf(p -> p.isEmpty());
    	}
    	catch(Exception e) {
    		errors.add(e.getMessage());
    	}
    	
    	if(errors.size() == 0)
    		errors = null;
    	
    	return new SearchResult(errors, results, containsCreate.size() > 0 ? containsCreate : null);
    }
    
    public List<Card> loadCards() throws Exception {
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
    
    public List<Card> loadTokens() throws Exception {
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
    
    public Card findCardByName(List<Card> cards, String name) {
    	Card ret = null;
    	Card backup = null;
    	
    	for (Iterator<Card> i = cards.iterator(); i.hasNext();) {
    		Card c = i.next();
    		
    		if(c.name.equalsIgnoreCase(name)) {
    			if(ret == null)
    				ret = c;
    			else if(c.all_parts != null) {
					ret = c;
    			}
    		}
    		else if(StringUtils.containsIgnoreCase(c.name, name)) {
    			if(backup == null)
    				backup = c;
    			else if(c.all_parts != null) {
    				backup = c;
    			}
    		}
    		
    	}
    	return ret != null? ret : backup;
    }
    
    public Card findToken(List<Card> cards, String scryfall_id) {
    	for (Iterator<Card> i = cards.iterator(); i.hasNext();) {
    		Card c = i.next();
    		
    		if(c.id.equals(scryfall_id))
    			return c;
    	}
    	return null;
    }
    
    public List<TokenResult> addTokenAndSources(List<TokenResult> results, Card token, Card source) {
    	boolean found = false;
    	
    	//Determine image to use for source
    	try {
	    	if(source.image_uris != null)
	    		source.small_image = source.image_uris.small;
	    	else if(source.card_faces != null)
	    		source.small_image = source.card_faces.get(0).image_uris.small;
    	}
    	catch(Exception e) {
    		source.small_image = "";
    	}
    	
    	for (Iterator<TokenResult> i = results.iterator(); i.hasNext();) {
    		TokenResult tr = i.next();
    		if(tr.token.oracle_id.equals(token.oracle_id)) {
    			found = true;
    			tr.sources.add(source);
    		}
    	}
    	
    	if(found == false) {
    		results.add(new TokenResult(token, source, ""));
    	}
    	
    	return results;
    }
}