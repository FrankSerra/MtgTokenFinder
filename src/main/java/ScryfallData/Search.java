package ScryfallData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import HelperObjects.ImageSize;
import HelperObjects.MatchType;
import HelperObjects.OracleTextHelper;
import HelperObjects.RegexHelper;
import HelperObjects.SearchHelper;
import ThymeleafEntities.ContainsCreateResult;
import ThymeleafEntities.SearchResult;
import ThymeleafEntities.TokenByNameResult;
import ThymeleafEntities.TokenByNameResultsContainer;
import ThymeleafEntities.TokenGuess;
import ThymeleafEntities.TokenPrintingsResult;
import ThymeleafEntities.TokenResult;

public class Search {
	private static ArrayList<Card> copyToken = null;
	private static ArrayList<Card> amassToken = null;
	private static ArrayList<Card> treasureToken = null;
	
	private static ArrayList<String> search_string_remove_duplicates(ArrayList<String> terms) {
		//Use a Set to remove duplicates, then put back into a sorted List
		HashSet<String> uniqueTerms = new HashSet<String> (terms);
		terms.clear();
		terms.addAll(uniqueTerms);
		
		//Prepare search terms
		String[] termsArray = terms.toArray(new String[0]);
		for(int i=0; i<termsArray.length; i++) {
			termsArray[i] = SearchHelper.prepareSearchTerm(termsArray[i]);
		}
		
		terms = new ArrayList<String>();
		terms.addAll(Arrays.asList(termsArray));
		
		//Clear empties, then sort
		terms.removeIf(p -> p.isEmpty());
		Collections.sort(terms);	
		
		return terms;
	}
	
	private static ArrayList<String> search_string_to_list(String cardlist) {
    	//Search terms
		ArrayList<String> terms = new ArrayList<String>();
		terms.addAll(Arrays.asList(cardlist.split("\\s*\\r?\\n\\s*")));
		
		//Clear obvious list cuts
		terms.removeIf(p -> StringUtils.containsIgnoreCase(p, "sideboard"));
		terms.removeIf(p -> StringUtils.containsIgnoreCase(p, "maybeboard"));			
		
		return search_string_remove_duplicates(terms);
	}
	
	private static void processForTipCards(ScryfallDataManager sdm, SearchResult searchResult, Card found) {
		//Check for tip cards
		if(found != null) {
			if(OracleTextHelper.oracle_text_contains(found, "experience counter"))
				SearchHelper.addTokenAndSources(searchResult, SearchHelper.findTipCard(sdm.tipcards, "Experience Counter"), found);
			
			if(OracleTextHelper.oracle_text_contains(found, "the monarch"))
				SearchHelper.addTokenAndSources(searchResult, SearchHelper.findTipCard(sdm.tipcards, "The Monarch"), found);
			
			if(OracleTextHelper.oracle_text_contains(found, "{E}"))
				SearchHelper.addTokenAndSources(searchResult, SearchHelper.findTipCard(sdm.tipcards, "Energy Reserve"), found);
			
			if(OracleTextHelper.oracle_text_contains(found, "the city's blessing"))
				SearchHelper.addTokenAndSources(searchResult, SearchHelper.findTipCard(sdm.tipcards, "City's Blessing"), found);
			
			if(OracleTextHelper.oracle_text_contains(found, "infect ") || 
			   OracleTextHelper.oracle_text_contains(found, "infect.") || 
			   OracleTextHelper.oracle_text_contains(found, "poison counter"))
				SearchHelper.addTokenAndSources(searchResult, SearchHelper.findTipCard(sdm.tipcards, "Poison Counter"), found);					
			
			if(OracleTextHelper.oracle_text_contains(found, "manifest th") || 
			   OracleTextHelper.oracle_text_contains(found, "manifest one") || 
			   OracleTextHelper.oracle_text_contains(found, "manifests "))
				SearchHelper.addTokenAndSources(searchResult, SearchHelper.findTipCard(sdm.tipcards, "Manifest"), found);
			
			if(OracleTextHelper.oracle_text_contains(found, "megamorph ") || 
			   OracleTextHelper.oracle_text_contains_regex_multiline(found, "^Morph "))
				SearchHelper.addTokenAndSources(searchResult, SearchHelper.findTipCard(sdm.tipcards, "Morph"), found);
		}		
	}
	
	private static void processForManuallyCheckedTokens(ScryfallDataManager sdm, SearchResult searchResult, Card cc) {
		//Check for copy tokens
		if(OracleTextHelper.oracle_text_contains(cc, "that's a copy of") || OracleTextHelper.oracle_text_contains(cc, "that are copies of")) {

			//Strip the copy text so it won't show up later
			Pattern pattern = Pattern.compile("(C|c)reate.*cop(y|ies)");
			if(cc.oracle_text != null) {
				Matcher m = pattern.matcher(cc.oracle_text);
				while(m.find()) {
					cc.oracle_text = cc.oracle_text.substring(0, m.start()) + cc.oracle_text.substring(m.end());
				}
			}
			else {
				for(CardFace cf: cc.card_faces) {
					Matcher m = pattern.matcher(cf.oracle_text);
					while(m.find()) {
						cf.oracle_text = cf.oracle_text.substring(0, m.start()) + cf.oracle_text.substring(m.end());
					}
				}
			}
			
			if(copyToken == null)
				copyToken = SearchHelper.findTokensByName(sdm.tokens, "Copy", null, null, true);
			
			if(copyToken != null && copyToken.size() > 0)
				SearchHelper.addTokenAndSources(searchResult, copyToken.get(0), cc);	
		}
		
		//Check for amass tokens
		if(OracleTextHelper.oracle_text_contains(cc, "amass ")) {
			if(amassToken == null)
				amassToken = SearchHelper.findTokensByName(sdm.tokens, "Zombie Army", "0", "0", true);
			
			if(amassToken != null && amassToken.size() > 0)
				SearchHelper.addTokenAndSources(searchResult, amassToken.get(0), cc);
		}		
		
		//Check for Treasure tokens because Smothering Tithe is the bane of my existence
		if(OracleTextHelper.oracle_text_contains(cc, " Treasure token")) {
			if(treasureToken == null)
				treasureToken = SearchHelper.findTokensByName(sdm.tokens, "Treasure", null, null, true);
			
			if(treasureToken != null && treasureToken.size() > 0)
				SearchHelper.addTokenAndSources(searchResult, treasureToken.get(0), cc);
		}
	}
	
	private static void processTokenGuesses(ScryfallDataManager sdm, SearchResult searchResult, Card cc) {
		ArrayList<Card> guess = new ArrayList<Card>();
		boolean			foundGuesses = false;
		
		//Calculated image links
    	cc.calculated_small  = ScryfallDataManager.getImageApiURL(cc, ImageSize.small, false);
    	cc.calculated_normal = ScryfallDataManager.getImageApiURL(cc, ImageSize.normal, false);
		
		for(TokenGuess tg : SearchHelper.prepareTokenGuess(cc)) {
			foundGuesses = true;
			if(!tg.name.isEmpty()) {
				guess = SearchHelper.findTokensByName(sdm.tokens, tg.name, tg.power, tg.toughness, false);
		    }
			
			//Check for emblems
			if(OracleTextHelper.oracle_text_contains(cc, "emblem ")) {
				//Emblems need to search for each individual card face
				
				//If single-face card
				if(cc.card_faces == null) {
					boolean hasEmblem = false;
					for(Card gc: guess) {
						if(gc.name.equals(cc.name + " Emblem"))
							hasEmblem = true;
					}
					if(!hasEmblem) {
						List<Card> g = SearchHelper.findTokensByName(sdm.tokens, cc.name+" Emblem", null, null, true);
						if(g != null) {
							SearchHelper.addTokenAndSources(searchResult, g.get(0), cc);
						}
					}
				}
				//If card is double-faced
				else {
					for(CardFace cf: cc.card_faces) {
						boolean hasEmblem = false;
						for(Card gc: guess) {
							if(gc.name.equals(cf.name + " Emblem"))
								hasEmblem = true;
						}
						if(!hasEmblem) {
							List<Card> g = SearchHelper.findTokensByName(sdm.tokens, cc.name+" Emblem", null, null, true);
							if(g != null) {
								SearchHelper.addTokenAndSources(searchResult, g.get(0), cc);
							}
						}
					}
				}
			}

	    	//Perform confidence matching
			boolean confidant = false;
			for(Card tokenCheck : guess) {
				if(SearchHelper.cardContainsTokenPhrase(cc, tokenCheck)) {
					confidant = true;
					SearchHelper.addTokenAndSources(searchResult, tokenCheck, cc);
					break;
				}
			}

			if(!confidant)
				searchResult.containsCreate.add(new ContainsCreateResult(cc, guess, ""));
		}
		
		if(!foundGuesses) {
			searchResult.containsCreate.add(new ContainsCreateResult(cc, guess, ""));
		}
	}
	
	private static void processToRemoveSolvedGuesses(SearchResult searchResult) {
		//For each contains create object with no guesses attached:
		//Count number of times the word "create" appears (X)
		//Count number of times the card appears in the token sources list (Y)
		//If Y >= X, cut it from the list because all of the unknown tokens were confidence-matched
		Iterator<ContainsCreateResult> it = searchResult.containsCreate.iterator();
		while(it.hasNext()) {
			ContainsCreateResult ccr = it.next();
			int create_appears = OracleTextHelper.oracle_text_contains_create_count(ccr.card);
			int source_count = 0;
			for(TokenResult tr: searchResult.tokenResults) {
				if(tr.sources.contains(ccr.card))
					source_count++;
			}
			
			if(source_count >= create_appears)
				it.remove();
		}
	}
	
	public static TokenPrintingsResult tokenPrintings(String oracleid, int face) {
		ScryfallDataManager sdm = new ScryfallDataManager(true);
		ArrayList<Card>		results = null;
		Card 				firstResult = null;
		
		//find first result for oracleid
		for(Card t: sdm.tokens) {
			if(t.oracle_id.equals(oracleid)) {
				firstResult = t;
				break;
			}
		}
		
		if(firstResult != null) {
			if(firstResult.card_faces == null) {
				face = -1;
			}
			
			results = SearchHelper.findTokenPrintingsByName(sdm.tokens, firstResult, face);
		}
				
		return new TokenPrintingsResult(firstResult.getTokenSummaryTitle(face), results);
	}

	public static TokenByNameResultsContainer tokenNameSearchResults(String tokenlist) {
		ScryfallDataManager sdm = new ScryfallDataManager(false);
		ArrayList<TokenByNameResult> results = new ArrayList<TokenByNameResult>();
		
		ArrayList<String> terms = new ArrayList<String>();
		ArrayList<String> notfound = new ArrayList<String>();
		terms.addAll(Arrays.asList(tokenlist.split("\n")));
		terms = search_string_remove_duplicates(terms);
				
		for(String t: terms) {
			String[] term = RegexHelper.extractPowerToughness(t);
			TokenByNameResult tbnr = new TokenByNameResult(t);
			
			ArrayList<Card> guesses = SearchHelper.findTokensByName(sdm.tokens, term[2].trim(), term[0], term[1], false, true);			
			
			Iterator<Card> it = guesses.iterator();
			while(it.hasNext()) {
				Card c = it.next();
				boolean add = true;
				
				for(Card tok: tbnr.results) {
					if(MatchType.doesTokenPrintingMatch(tok, c, c.matching_face).match) {
						add = false;
					}
				}
				
				if(add) {
					int paren = c.display_name.indexOf("(");
					if(paren > -1) {
						c.display_name = c.display_name.substring(0, paren-1);
					}
					tbnr.results.add(c);
				}
			}
			
			if(tbnr.results.size() > 0)
				results.add(tbnr);
			else
				notfound.add(t);
		}
		
		return new TokenByNameResultsContainer(results, terms, notfound);
	}
	
	public static SearchResult tokenResults(String cardlist, boolean matchExact, boolean includeSilver) {
		ScryfallDataManager sdm = new ScryfallDataManager(includeSilver);
		SearchResult searchResult = new SearchResult();

		List<Card> containsCreate = new ArrayList<Card>();
    	
    	try { 		
			for (String term: search_string_to_list(cardlist)) {
				Card found = SearchHelper.findCardByName(sdm.cards, matchExact, term, false);
				if(found == null) {
					found = SearchHelper.findCardByName(sdm.cards, matchExact, term, true);
				}
				
				if(found == null) {
					searchResult.errors.add(term);
				}
				else {					
					searchResult.full_list.add(term);
					if(found.all_parts != null) {			
						boolean goteem = false;
						for (Related_Card rc: found.all_parts) {
							Card token = SearchHelper.findToken(sdm.tokens, rc.id);
							if(token != null) {
								goteem = true;
								SearchHelper.addTokenAndSources(searchResult, token, found);	
								if(OracleTextHelper.oracle_text_contains_create_multiple(found))
									containsCreate.add(found);
							}
						}
						if(!goteem && OracleTextHelper.oracle_text_contains_create(found)) {
							containsCreate.add(found);
						}
					}
					else if(OracleTextHelper.oracle_text_contains_create(found)) {
						containsCreate.add(found);
					}
					
					//Tip cards are never officially related - have to search for them on every card
					processForTipCards(sdm, searchResult, found);
				}
			}
			
			//Additional processing for cards that don't have every token officially related
			for(Card cc : containsCreate) {
						
				processForManuallyCheckedTokens(sdm, searchResult, cc);
				
				processTokenGuesses(sdm, searchResult, cc);   
			}
			
			//Remove guesses that are already solved
			processToRemoveSolvedGuesses(searchResult);
    	}
    	catch(Exception e) {  		
    		searchResult.errors.add("ERROR: " + e.getMessage());
    		e.printStackTrace();
    	}
		
    	searchResult.errors.removeIf(p -> p.isEmpty());

    	if(searchResult.errors.size() == 0)
    		searchResult.errors = null;
    	
    	return searchResult;
    }
}
