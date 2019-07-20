package HelperObjects;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import ScryfallData.Card;
import ScryfallData.ScryfallDataManager;
import ThymeleafEntities.SearchResult;
import ThymeleafEntities.TokenGuess;
import ThymeleafEntities.TokenResult;

public class SearchHelper {
	
	public static Card findCardByName(List<Card> cards, boolean matchExact, String name, boolean stripAccents) {
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
    		else if(stripAccents && StringUtils.stripAccents(c.name).equalsIgnoreCase(StringUtils.stripAccents(name))) {
    			ret = c;
    		}
    		else if(!matchExact && StringUtils.containsIgnoreCase(c.name, name)) {
    			if(backup == null)
    				backup = c;
    			else if(c.all_parts != null) {
    				backup = c;
    			}
    		}
    		else if(c.card_faces != null) {
    			for(int x=0; x<c.card_faces.size(); x++) {
    				if(c.card_faces.get(x).name.equalsIgnoreCase(name)) {
    					backup = c;
    				}
    			}
    		}
    	}
    	return ret != null? ret : backup;
    }
    
    public static Card findToken(List<Card> cards, String scryfall_id) {
    	for (Iterator<Card> i = cards.iterator(); i.hasNext();) {
    		Card c = i.next();
    		
    		if(c.id.equals(scryfall_id))
    			return c;
    	}
    	return null;
    }
        
    public static ArrayList<Card> findTokensByName(List<Card> cards, String name, String power, String toughness, boolean firstOnly) {
    	return findTokensByName(cards, name, power, toughness, firstOnly, false);
    }
   
    public static ArrayList<Card> findTokensByName(List<Card> cards, String name, String power, String toughness, boolean firstOnly, boolean ignoreCase) {
    	ArrayList<Card> matches = new ArrayList<Card>();
    	Set<String> ids = new HashSet<String>();
    	
    	for (Iterator<Card> i = cards.iterator(); i.hasNext();) {
    		Card c = i.next();
    		
    		MatchType match = MatchType.doesTokenMatch(c, name, power, toughness, ignoreCase, true);
			if(match.match == true && ids.add(c.oracle_id)) {
				String disp = "";
				
				if(c.getPower(match.card_face) != null) {
					disp += c.getPower(match.card_face) + "/" + c.getToughness(match.card_face) + " ";
				}
				
				disp += c.buildColorPhrase(c.getColors(match.card_face));
				
				disp += " " + c.getTypes(match.card_face);
				
				disp += " " + c.getName(match.card_face);
				
				String oracle = c.getOracle(match.card_face);
				if(oracle !=null && !oracle.isEmpty()) {
					c.display_oracle = oracle;
				}
				
				c.display_name = disp;
				
				c.calculated_small = ScryfallDataManager.getImageApiURL(c, HelperObjects.ImageSize.small, match.card_face == 1);
				c.calculated_normal = ScryfallDataManager.getImageApiURL(c, HelperObjects.ImageSize.normal, match.card_face == 1);
				
				c.matching_face = match.card_face;
				matches.add(c);
				
				if(firstOnly)
					break;
			}
		}

    	return matches;
    }
    
    public static ArrayList<Card> findTokenPrintingsByName(List<Card> cards, Card firstResult, int face) {
    	ArrayList<Card> matches = new ArrayList<Card>();
    	
	   	for (Iterator<Card> i = cards.iterator(); i.hasNext();) {
    		Card c = i.next();
    		
    		MatchType match = MatchType.doesTokenPrintingMatch(c, firstResult, face);
			if(match.match == true) {
				c.calculated_small = ScryfallDataManager.getImageApiURL(c, HelperObjects.ImageSize.small, match.card_face == 1);
				c.calculated_normal = ScryfallDataManager.getImageApiURL(c, HelperObjects.ImageSize.normal, match.card_face == 1);
				
				if(c.set.substring(0,1).equals("t")) {
					c.set = c.set.substring(1);
				}
				
				c.set = c.set.toUpperCase();
				
				String mods = "";
				if(c.foil) {
					mods += "FOIL ";
				}
				if(c.card_faces != null) {
					mods += "DFC ";
				}
				
				if(!mods.isEmpty()) {
					c.set += " (" + mods.trim() + ")";
				}
				
				matches.add(c);
			}
		}
    	
    	return matches;
    }
    
    public static Card findTipCard(ArrayList<Card> tipcards, String name) {
    	try {
			for(Card c: tipcards) {
				if(c.name.equalsIgnoreCase(name))
					return c;
			}
		} catch (Exception e) {
			return null;
		}
    	
    	return null;
    }
    
    public static void addTokenAndSources(SearchResult searchResult, Card token, Card source) {
    	boolean found = false;
    	
    	//Determine which card face is being used and trim the other side of the DFC token
    	int matched_face = 99;
    	if(token.card_faces != null) {
    		for(int i=0; i<token.card_faces.size();i++) {
    			if(source.oracle_text.toLowerCase().contains(token.card_faces.get(i).name.toLowerCase())) {
    				matched_face = i;
    				break;
    			}
    		}
    	}
    	
		//Calculated image links
		token.calculated_small  = ScryfallDataManager.getImageApiURL(token, HelperObjects.ImageSize.small, matched_face==1);
		token.calculated_normal = ScryfallDataManager.getImageApiURL(token, HelperObjects.ImageSize.normal, matched_face==1);
		
		source.calculated_small  = ScryfallDataManager.getImageApiURL(source, HelperObjects.ImageSize.small, matched_face==1);
		source.calculated_normal = ScryfallDataManager.getImageApiURL(source, HelperObjects.ImageSize.normal, matched_face==1);
    	
		for (TokenResult tr: searchResult.tokenResults) {
			if(tr.token.oracle_id.equals(token.oracle_id)) {
				found = true;
				if(!tr.sources.contains(source))
					tr.sources.add(source);
			}
    	}
    	
    	if(found == false) {
    		searchResult.tokenResults.add(new TokenResult(token, source, ""));
    	}
    }
    
    public static String prepareSearchTerm(String term) {
    	term = StringEscapeUtils.unescapeHtml4(term).trim();
		term = term.replace("\r", "").replace("SB: ", "").replaceAll("^[0-9]+\\w* ", "").trim();
		
		//Return empty string for common notations of comments, or if the line has no letters in it at all
		if(term.matches("^\\/\\/.*") || term.matches("^#.*") || !term.matches(".*[a-zA-Z].*"))
			return "";
		
		Matcher captureBetweenParens = Pattern.compile("(?<=(\\)|\\]))[\\w,'\" -_:!\\?&]+").matcher(term);
		if(captureBetweenParens.find()) {
			term = captureBetweenParens.group();
		}
		
		captureBetweenParens = Pattern.compile(".*(?=(\\(|\\[))").matcher(term);
		if(captureBetweenParens.find()) {
			term = captureBetweenParens.group();
		}
		
		Pattern symbolCutPattern = Pattern.compile("[\\(\\[\\*#]");
		Matcher symbolCut = symbolCutPattern.matcher(term);
		
		if(symbolCut.find()) {
			term = term.substring(0, symbolCut.start());
		}
		
		term = term.replace(" / ", " // ");
		
		return term.trim();
    }
    
    public static List<TokenGuess> prepareTokenGuess(Card cc) {
    	String 				search_text = cc.oracle_text;
    	List<TokenGuess> 	all_guesses = new ArrayList<TokenGuess>();
    	TokenGuess			currentGuess;
    	String 				tokenName = "";
        String 				power = null, toughness = null;
        
        //Need to explicitly pull "tokens named" first, otherwise they won't be processed right
    	Pattern patternNamed = Pattern.compile("(?<=(C|c)reates? )(.*)(?= tokens? named )");
        Matcher matcherNamed = patternNamed.matcher(search_text);
	    while(matcherNamed.find()) {
	    	String tokenClause = matcherNamed.group();
	    	search_text = search_text.substring(0, matcherNamed.start()) + search_text.substring(matcherNamed.end());
	    	
	    	//If it's a creature token, get the P/T declaration
	    	String[] powtough = RegexHelper.extractPowerToughness(tokenClause);
	    	power       = powtough[0];
	    	toughness   = powtough[1];
	    	tokenClause = powtough[2];
	    	
	    	//Check if this uses the "token named N" oracle text pattern
	    	Pattern namedN = Pattern.compile("(?<=token named ).*");
	    	Matcher namedNmatch = namedN.matcher(cc.oracle_text);
	    	if(namedNmatch.find()) {
	    		tokenClause = namedNmatch.group();
	    	}	 
	    	
	    	//Determine final search name
	    	tokenName = RegexHelper.extractName(tokenClause);
	        if(tokenName != null) {
		        currentGuess = new TokenGuess(tokenName, power, toughness);
			    if(!all_guesses.contains(currentGuess)) {
			    	all_guesses.add(currentGuess);
			    }
	        }
	    }
	    
        //Match normal tokens
    	Pattern pattern = Pattern.compile("(?<=(C|c)reates? )(.*)(?= token)");
        Matcher matcher = pattern.matcher(search_text);
	    while(matcher.find()) {
	    	String tokenClause = matcher.group();  
	    	
	    	//If it's a creature token, get the P/T declaration
	    	String[] powtough = RegexHelper.extractPowerToughness(tokenClause);
	    	power       = powtough[0];
	    	toughness   = powtough[1];
	    	tokenClause = powtough[2];
	    	
	    	tokenName = RegexHelper.extractName(tokenClause);
	        if(tokenName != null) {
		        currentGuess = new TokenGuess(tokenName, power, toughness);
			    if(!all_guesses.contains(currentGuess)) {
			    	all_guesses.add(currentGuess);
			    }
	        }
	    }
	    
	    return all_guesses;
    }
    
    public static String letterToWord(String s) {
    	switch(s) {
		case "W":
		case "w":
			return "White";
		case "U":
		case "u":
			return "Blue";
		case "B":
		case "b":
			return "Black";
		case "R":
		case "r":
			return "Red";
		case "G":
		case "g":
			return "Green";
		default:
			return "";
		}
    }
    
    public static boolean cardContainsTokenPhrase(Card card, Card token) {
    	int face = token.matching_face;
    	
    	//Check for Treasure tokens manually because Smothering Tithe is the hardest edge case I've ever encountered
    	//It is not possible to construct a regex statement that satisfies Smothering Tithe while also satisfying normal creature text
		if(token.name.equals("Treasure")) {
			return OracleTextHelper.oracle_text_contains(card, " Treasure token");
		}
		
    	//Oracle phrasing is one of: 
		//P/T -> colors -> name -> types -> "with XYZ"
		//P/T -> colors -> types -> "named " name
		String name = token.getName(face);
		String typeline = token.getFullTypeline(face);
		
		String disp = ".*";
		if(token.power != null) {
			disp += token.getPower(face) + "\\/" + token.getToughness(face);
		}
		disp += " " + token.buildOracleColorPhrase(token.getColors(face));			
		
		//If the token name is not fully contained in the token typeline, that means it's a "named N" format. 
		//If it is, use the standard format.
		if(typeline.toLowerCase().contains(name.toLowerCase())) {
			disp += " " + token.getName(face);
			
			disp += " " + token.getTypes(face).toLowerCase();
			
			disp += " tokens?";
			
			String oracle = token.getOracle(face);
			if(oracle != null && !oracle.isEmpty()) {
				oracle = oracle.replace("{", "\\{").replace("}", "\\}").replace("*", "\\*");
				
				//If token has reminder text, don't match against it
				int idx = oracle.indexOf("(");
				if(idx >= 0) {
					oracle = oracle.substring(0, idx);
				}
				
				disp += " with \"?" + oracle.trim() + "\"?";
			}
			else {
				//If a token is vanilla, it has to match the negative lookahead of "with", meaning the card doesn't say that the token has any text.
				disp += ".(?!with )";
			}
			disp += ".*";

		}
		else {
			//If a token has a fancy name, we don't have to get weird, we just need the token's proper name in the card text
			disp += ".* " + token.getTypes(face).toLowerCase();
			disp += " tokens?";
			disp += ".* named " + token.getName(face) + ".*";
		}

		return OracleTextHelper.oracle_text_contains_regex(card, MatchType.getOracleTextConversion(disp, true));
    }
	   
}
