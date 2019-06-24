package tokenfinder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@Controller
public class CardController {

    @GetMapping({"/", "/tokens", "/deckbox"})
    public String cards(Model model) {
        return "redirect:/search";
    }
    
    @GetMapping("/search")
    public String search(Model model) {
        return "search";
    }
    
    @GetMapping("/about")
    public String about(Model model) {
        return "about";
    }
    
    @PostMapping("/tokens")
    public String tokens(@RequestParam(name="cardlist", required=true, defaultValue="") String cardlist, Model model) {
    	SearchResult sr = tokenResults(cardlist);
    	model.addAttribute("cardlist", sr.errors);
    	model.addAttribute("results", sr.tokenResults.size() > 0 ? sr.tokenResults : null);
    	model.addAttribute("contains_create", sr.containsCreate);
    	
    	return "tokens";
    }
    
    @PostMapping("/deckbox")
    public String deckbox(@RequestParam(name="deckboxurl", required=true, defaultValue="") String deckboxurl, Model model) {
    	if(!deckboxurl.endsWith("/export"))
    		deckboxurl = deckboxurl + "/export";
    	
    	Document doc = null;
		try {
			doc = Jsoup.connect(deckboxurl).get();
		} catch (IOException e) {
			model.addAttribute("error", "Unable to connect to Deckbox.org - try again later.");
			return "error";
		}
    	
		Element cards = doc.select("body").first();
		doc.select("p").remove();
    	String list = cards.html();
    	list = list.replace("<br>", "\n");
    	
		return tokens(list, model);
    }
    
    public SearchResult tokenResults(String cardlist) {
    	List<String> errors = new ArrayList<String>();
    	List<TokenResult> results = new ArrayList<TokenResult>();
    	List<Card> containsCreate = new ArrayList<Card>();
    	List<ContainsCreateResult> ccResults = new ArrayList<ContainsCreateResult>();
    	
    	try { 		
	    	List<Card> cards = loadCards();
	    	List<Card> tokens = loadTokens();
	    	
	    	//Search terms
			String[] terms = cardlist.split("\\n");
			for (String term: terms) {
				term = term.replace("\r", "").replaceAll("[0-9]+\\w* ", "").trim();
				
				int cut = Math.min(term.indexOf("("), term.indexOf("["));
				if(cut >= 0)
					term = term.substring(0, cut).trim();
				
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
					if(!goteem && this.oracle_text_contains_create(found)) {
						containsCreate.add(found);
					}
				}
				else if(found != null && this.oracle_text_contains_create(found)) {
					containsCreate.add(found);
				}
				else if(found == null){
					errors.add(term);
				}
			}
			
			//Process containsCreate for token guesses			
			for(Card cc : containsCreate) {
				Pattern pattern = Pattern.compile("(?<=(C|c)reates? )(.*)(?= token)");
		        Matcher matcher = pattern.matcher(cc.oracle_text);
		        List<Card> guess = null;
		        
			    if(matcher.find()) {
			    	String tokenClause = cc.oracle_text.substring(matcher.start(), matcher.end());
			    	Pattern clausePattern = Pattern.compile("\\b[A-Z].*?\\b");
			        Matcher clauseMatcher = clausePattern.matcher(tokenClause);
			    	
			        if(clauseMatcher.find()) { 
			        	String tokenName = tokenClause.substring(clauseMatcher.start(), clauseMatcher.end());
			        	guess = findTokensByName(tokens, tokenName);
			        }
		        }
			    
			    ccResults.add(new ContainsCreateResult(cc, guess, ""));
			}
			
			errors.removeIf(p -> p.isEmpty());
    	}
    	catch(Exception e) {
    		errors.add(e.getMessage());
    	}
    	
    	if(errors.size() == 0)
    		errors = null;
    	
    	return new SearchResult(errors, results, ccResults);
    }
    
    public boolean oracle_text_contains_create(Card c) {
    	if(c.oracle_text != null) {
    		return (StringUtils.containsIgnoreCase(c.oracle_text, " create") || c.oracle_text.startsWith("Create"));
    	}
    	else if(c.card_faces.size() > 0) {
    		for (CardFace face : c.card_faces) {
				if(StringUtils.containsIgnoreCase(face.oracle_text, " create") || face.oracle_text.startsWith("Create"))
					return true;
			}
    	}
    	
    	return false;
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
    
    public List<Card> findTokensByName(List<Card> cards, String name) {
    	List<Card> matches = new ArrayList<Card>();
    	Set<String> ids = new HashSet<String>();
    	
    	for (Iterator<Card> i = cards.iterator(); i.hasNext();) {
    		Card c = i.next();
    		
    		if(c.name.equals(name)) {
    			if(ids.add(c.oracle_id)) {
    				String disp = "";
    				if(c.power != null) {
    					disp += c.power + "/" + c.toughness + " ";
    				}
    				
    				if(c.color_identity.size() == 0) {
    					disp += "Colorless ";
    				}
    				else {
    					for(int idx=0; idx<c.color_identity.size(); idx++) {
    						if(idx > 0)
    							disp += "-";
    						
        					switch(c.color_identity.get(idx)) {
        					case "W":
        						disp += "White";
        						break;
        					case "U":
        						disp += "Blue";
        						break;
        					case "B":
        						disp += "Black";
        						break;
        					case "R":
        						disp += "Red";
        						break;
        					case "G":
        						disp += "Green";
        						break;
        					}
        				}
    				}
    				
    				disp += " " + c.name;
    				
    				if(!c.oracle_text.isEmpty()) {
    					disp += " with " + c.oracle_text;
    				}
    				
    				c.display_name = disp;
					matches.add(c);
    			}
    		}
    	}

    	return matches;
    }
    
    public List<TokenResult> addTokenAndSources(List<TokenResult> results, Card token, Card source) {
    	boolean found = false;
    	
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