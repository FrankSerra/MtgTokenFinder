package tokenfinder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import tokenfinder.ScryfallDataManager.ImageSize;

public class SearchHelper {
	
	public static boolean oracle_text_contains_create(Card c) {
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
	
	public static boolean oracle_text_contains(Card c, String text) {
    	if(c.oracle_text != null) {
    		return (StringUtils.containsIgnoreCase(c.oracle_text, text));
    	}
    	else if(c.card_faces.size() > 0) {
    		for (CardFace face : c.card_faces) {
				if(StringUtils.containsIgnoreCase(face.oracle_text, text))
					return true;
			}
    	}
    	
    	return false;
    }
	
	public static boolean oracle_text_contains_regex(Card c, String text) {
		//Pattern search = Pattern.compile(text, Pattern.CASE_INSENSITIVE);
		if(c.oracle_text != null) {
    		return c.oracle_text.replace("\r", "").replace("\n", "").matches("(?i)"+text);
			//return search.matcher(c.oracle_text.replace("\r", "").replace("\n", "")).find();
    	}
    	else if(c.card_faces.size() > 0) {
    		for (CardFace face : c.card_faces) {
				if(face.oracle_text.replace("\r", "").replace("\n", "").matches(text))
					return true;
			}
    	}
    	
    	return false;
    }
	
	public static Card findCardByName(List<Card> cards, boolean matchExact, String name) {
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
    	ArrayList<Card> matches = new ArrayList<Card>();
    	Set<String> ids = new HashSet<String>();
    	
    	for (Iterator<Card> i = cards.iterator(); i.hasNext();) {
    		Card c = i.next();
    		
    		MatchType match = MatchType.doesTokenMatch(c, name, power, toughness);
			if(match.match == true && ids.add(c.oracle_id)) {
				String disp = "";
				
				if(power != null) {
					disp += c.getPower(match.card_face) + "/" + c.getToughness(match.card_face) + " ";
				}
				
				disp += c.buildColorPhrase(c.getColors(match.card_face));
				
				disp += " " + c.getTypes(match.card_face);
				
				disp += " " + c.getName(match.card_face);
				
				String oracle = c.getOracle(match.card_face);
				if(!oracle.isEmpty()) {
					c.display_oracle = oracle;
				}
				
				c.display_name = disp;
				
				c.calculated_small = ScryfallDataManager.getImageApiURL(c, ImageSize.small, match.card_face == 1);
				
				matches.add(c);
				
				if(firstOnly)
					break;
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
    
    public static List<TokenResult> addTokenAndSources(List<TokenResult> results, Card token, Card source) {
    	boolean found = false;
    	
    	if(token == null)
    		return results;
    	
    	//Determine which card face is being used and trim the other side of the DFC token
    	int trimmed_face = 99;
    	if(token.card_faces != null) {
    		for(int i=0; i<token.card_faces.size();i++) {
    			if(!source.oracle_text.contains(token.card_faces.get(i).name)) {
    				trimmed_face = i;
    				token.trimCardFace(i);
    			}
    		}
    	}
    	
		//Calculated image links
		token.calculated_small  = ScryfallDataManager.getImageApiURL(token, ImageSize.small, trimmed_face==1);
		token.calculated_normal = ScryfallDataManager.getImageApiURL(token, ImageSize.normal, trimmed_face==1);
		
		source.calculated_small  = ScryfallDataManager.getImageApiURL(source, ImageSize.small, trimmed_face==1);
		source.calculated_normal = ScryfallDataManager.getImageApiURL(source, ImageSize.normal, trimmed_face==1);
    	
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
    
    public static String prepareSearchTerm(String term) {
    	term = term.trim();
		term = term.replace("\r", "").replace("SB: ", "").replaceAll("^[0-9]+\\w* ", "").trim();
		
		//Return empty string for common notations of comments, or if the line has no letters in it at all
		if(term.matches("^\\/\\/.*") || term.matches("^#.*") || !term.matches("[a-zA-Z].*"))
			return "";
		
		Pattern symbolCutPattern = Pattern.compile("[\\(\\[\\*#]");
		Matcher symbolCut = symbolCutPattern.matcher(term);
		
		if(symbolCut.find()) {
			term = term.substring(0, symbolCut.start()).trim();
		}
		
		term = term.replace(" / ", " // ");
		
		return term;
    }
    
    public static List<TokenGuess> prepareTokenGuess(Card cc) {
    	List<TokenGuess> 	all_guesses = new ArrayList<TokenGuess>();
    	TokenGuess			currentGuess;
    	String 				tokenName = "";
    	Pattern 			pattern = Pattern.compile("(?<=(C|c)reates? )(.*)(?= token)");
        Matcher 			matcher = pattern.matcher(cc.oracle_text);
        String 				power = null, toughness = null;
        
	    while(matcher.find()) {
	    	String tokenClause = cc.oracle_text.substring(matcher.start(), matcher.end());  
	    	
	    	//If it's a creature token, get the P/T declaration
	    	Pattern ptOnly = Pattern.compile("[0-9xX\\*]+/[0-9xX\\*]+");
	    	Matcher ptOnlyMatcher = ptOnly.matcher(tokenClause);
	    	
	    	Pattern pt = Pattern.compile("(?<=[0-9xX\\*]/[0-9xX\\*])(.*)");
	    	Matcher ptMatcher = pt.matcher(tokenClause);
	    	if(ptMatcher.find()) {
	    		ptOnlyMatcher.find();
	    		String stats = tokenClause.substring(ptOnlyMatcher.start(), ptOnlyMatcher.end());
	    		tokenClause = tokenClause.substring(ptMatcher.start(), ptMatcher.end());
	    		
	    		power = stats.substring(0, stats.indexOf("/"));
	    		toughness = stats.substring(stats.indexOf("/")+1, stats.length());
	    	}
	    	
	    	//Check if this uses the "token named N" oracle text pattern
	    	Pattern namedN = Pattern.compile("(?<=token named ).*");
	    	Matcher namedNmatch = namedN.matcher(cc.oracle_text);
	    	if(namedNmatch.find()) {
	    		tokenClause = cc.oracle_text.substring(namedNmatch.start(), namedNmatch.end());
	    	}	 
	    	
	    	Pattern clausePattern = Pattern.compile("(\\b[A-Z].*?\\b)+( of | the )*(\\b[A-Z].*\\b)*");
	        Matcher clauseMatcher = clausePattern.matcher(tokenClause);
	        
	        if(clauseMatcher.find()) { 
	        	tokenName = tokenClause.substring(clauseMatcher.start(), clauseMatcher.end()).trim();
	        }
	    }
	    
	    currentGuess = new TokenGuess(tokenName, power, toughness);
	    if(!all_guesses.contains(currentGuess)) {
	    	all_guesses.add(currentGuess);
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
    	//Check for Treasure tokens manually because Smothering Tithe is the hardest edge case I've ever encountered
    	//It is not possible to construct a regex statement that satisfies Smothering Tithe while also satisfying normal creature text
		if(token.name.equals("Treasure")) {
			return SearchHelper.oracle_text_contains(card, " Treasure token");
		}
		
    	//Oracle phrasing is P/T -> colors -> name -> types -> "with XYZ"
    	String disp = ".*";
		
		if(token.power != null) {
			disp += token.getPower(-1) + "\\/" + token.getToughness(-1);
		}
		
		disp += " " + token.buildOracleColorPhrase(token.getColors(-1));
		
		disp += " " + token.getName(-1);
		
		disp += " " + token.getTypes(-1).toLowerCase();
		
		disp += " tokens?";
		
		String oracle = token.getOracle(-1);
		if(oracle != null && !oracle.isEmpty()) {
			disp += " with \"?" + oracle.replace("{", "\\{").replace("}", "\\}") + "\"?";
		}
		disp += ".*";
		
		System.out.print(disp);
		return SearchHelper.oracle_text_contains_regex(card, disp);
    }
	   
}
