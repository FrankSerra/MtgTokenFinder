package tokenfinder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

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
	
	public static Card findCardByName(List<Card> cards, String name) {
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
    
    public static Card findToken(List<Card> cards, String scryfall_id) {
    	for (Iterator<Card> i = cards.iterator(); i.hasNext();) {
    		Card c = i.next();
    		
    		if(c.id.equals(scryfall_id))
    			return c;
    	}
    	return null;
    }
        
    public static List<Card> findTokensByName(List<Card> cards, String name, String power, String toughness) {
    	List<Card> matches = new ArrayList<Card>();
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
				
				disp += " " + c.getName(match.card_face);
				
				String oracle = c.getOracle(match.card_face);
				if(!oracle.isEmpty()) {
					c.display_oracle = oracle;
				}
				
				c.display_name = disp;
				matches.add(c);
			}
		}

    	return matches;
    }
    
    public static List<TokenResult> addTokenAndSources(List<TokenResult> results, Card token, Card source) {
    	boolean found = false;
    	String root = "https://api.scryfall.com/cards/" + token.set + "/" + token.collector_number;
    	
		//Calculated image links
		token.calculated_small  = root + "?format=image&version=small";
		token.calculated_normal = root + "?format=image&version=normal";
    	
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
		term = term.replace("\r", "").replaceAll("^[0-9]+\\w* ", "").trim();
		
		int cutParen = term.indexOf("(");
		int cutBracket = term.indexOf("[");
		
		if(cutParen > -1 || cutBracket > -1) {
			if(cutBracket == -1)
				cutBracket = cutParen;
			
			if(cutParen == -1)
				cutParen = cutBracket;
			
			int cut = Math.min(cutParen, cutBracket);
			if(cut > -1)
				term = term.substring(0, cut).trim();
		}
		
		return term;
    }
    
    public static TokenGuess prepareTokenGuess(Card cc) {
    	String 		tokenName = "";
    	Pattern 	pattern = Pattern.compile("(?<=(C|c)reates? )(.*)(?= token)");
        Matcher 	matcher = pattern.matcher(cc.oracle_text);
        String 		power = null, toughness = null;
        
	    if(matcher.find()) {
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
	    	
	    	Pattern clausePattern = Pattern.compile("(\\b[A-Z].*?\\b )+(\\b[A-Z].*\\b)*");
	        Matcher clauseMatcher = clausePattern.matcher(tokenClause);
	        
	        if(clauseMatcher.find()) { 
	        	tokenName = tokenClause.substring(clauseMatcher.start(), clauseMatcher.end()).trim();
	        }
	    }
	    
	    return new TokenGuess(tokenName, power, toughness);
    }
	   
}
