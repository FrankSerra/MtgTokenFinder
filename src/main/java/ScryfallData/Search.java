package ScryfallData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
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
	private static ArrayList<Card> foodToken = null;
	
	private static ArrayList<String> search_string_remove_duplicates(ArrayList<String> terms) {
		return search_string_remove_duplicates(terms, true);
	}
	
	private static ArrayList<String> search_string_remove_duplicates(ArrayList<String> terms, boolean cutComments) {
		//Use a Set to remove duplicates, then put back into a sorted List
		HashSet<String> uniqueTerms = new HashSet<> (terms);
		terms.clear();
		terms.addAll(uniqueTerms);
		
		//Prepare search terms
		String[] termsArray = terms.toArray(new String[0]);
		for(int i=0; i<termsArray.length; i++) {
			termsArray[i] = SearchHelper.prepareSearchTerm(termsArray[i], cutComments);
		}
		
		terms = new ArrayList<>();
		terms.addAll(Arrays.asList(termsArray));
		
		//Clear empties, then sort
		terms.removeIf(String::isEmpty);
		Collections.sort(terms);	
		
		return terms;
	}
	
	private static ArrayList<String> search_string_to_list(String cardlist) {
    	//Search terms
		ArrayList<String> terms = new ArrayList<>(Arrays.asList(cardlist.split("\\s*\\r?\\n\\s*")));
		
		//Clear obvious list cuts
		terms.removeIf(p -> StringUtils.containsIgnoreCase(p, "sideboard"));
		terms.removeIf(p -> StringUtils.containsIgnoreCase(p, "maybeboard"));			
		
		return search_string_remove_duplicates(terms);
	}
	
	private static List<Card> findTipCards(ScryfallDataManager sdm, Card found) {
		List<Card> tipcards = new ArrayList<>();
		
		if(OracleTextHelper.oracle_text_contains(found, "experience counter"))
			tipcards.add(SearchHelper.findTipCard(sdm.tipcards, "Experience Counter"));
		
		if(OracleTextHelper.oracle_text_contains(found, "the monarch"))
			tipcards.add(SearchHelper.findTipCard(sdm.tipcards, "The Monarch"));
		
		if(OracleTextHelper.oracle_text_contains(found, "{E}"))
			tipcards.add(SearchHelper.findTipCard(sdm.tipcards, "Energy Reserve"));
		
		if(OracleTextHelper.oracle_text_contains(found, "the city's blessing"))
			tipcards.add(SearchHelper.findTipCard(sdm.tipcards, "City's Blessing"));
		
		if(OracleTextHelper.oracle_text_contains(found, "infect ") || 
		   OracleTextHelper.oracle_text_contains(found, "infect.") || 
		   OracleTextHelper.oracle_text_contains(found, "poison counter"))
			tipcards.add(SearchHelper.findTipCard(sdm.tipcards, "Poison Counter"));					
		
		if(OracleTextHelper.oracle_text_contains(found, "manifest th") || 
		   OracleTextHelper.oracle_text_contains(found, "manifest one") || 
		   OracleTextHelper.oracle_text_contains(found, "manifests "))
			tipcards.add(SearchHelper.findTipCard(sdm.tipcards, "Manifest"));
		
		if(OracleTextHelper.oracle_text_contains(found, "megamorph ") || 
		   OracleTextHelper.oracle_text_contains_regex_multiline(found, "^Morph "))
			tipcards.add(SearchHelper.findTipCard(sdm.tipcards, "Morph"));
		
		return tipcards;
	}
	
	private static void processForTipCards(ScryfallDataManager sdm, SearchResult searchResult, Card found) {
		//Check for tip cards
		if(found != null) {
			for(Card tip: findTipCards(sdm, found)) {
				SearchHelper.addTokenAndSources(searchResult, tip, found);
			}
		}		
	}
	
	private static List<Card> findManuallyCheckedTokens(ScryfallDataManager sdm, Card cc) {
		List<Card> manual_tokens = new ArrayList<>();
		
		//Check for copy tokens
		if(OracleTextHelper.oracle_text_contains(cc, "that's a copy of") || OracleTextHelper.oracle_text_contains(cc, "that are copies of")) {

			//Strip the copy text so it won't show up later
			Pattern pattern = Pattern.compile("[Cc]reate.*cop(y|ies)");
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
			
			if(copyToken.size() > 0)
				manual_tokens.add(copyToken.get(0));	
		}
		
		//Check for amass tokens
		if(OracleTextHelper.oracle_text_contains(cc, "amass ")) {
			if(amassToken == null)
				amassToken = SearchHelper.findTokensByName(sdm.tokens, "Zombie Army", "0", "0", true);
			
			if(amassToken.size() > 0)
				manual_tokens.add(amassToken.get(0));
		}		
		
		//Check for Treasure tokens because Smothering Tithe is the bane of my existence
		if(OracleTextHelper.oracle_text_contains(cc, " Treasure token")) {
			if(treasureToken == null)
				treasureToken = SearchHelper.findTokensByName(sdm.tokens, "Treasure", null, null, true);
			
			if(treasureToken.size() > 0)
				manual_tokens.add(treasureToken.get(0));
		}

		//Check for Food tokens because they have the same problem as Treasure
		if(OracleTextHelper.oracle_text_contains(cc, " Food token")) {
			if(foodToken == null)
				foodToken = SearchHelper.findTokensByName(sdm.tokens, "Food", null, null, true);

			if(foodToken.size() > 0) {
				manual_tokens.add(foodToken.get(0));
				OracleTextHelper.oracle_text_remove(cc, "[Cc]reate.* Food token");
			}
		}
		
		return manual_tokens;
	}
	
	private static void processForManuallyCheckedTokens(ScryfallDataManager sdm, SearchResult searchResult, Card cc) {
		for(Card token: findManuallyCheckedTokens(sdm, cc)) {
			SearchHelper.addTokenAndSources(searchResult, token, cc);
		}	
	}
	
	private static void processTokenGuesses(ScryfallDataManager sdm, SearchResult searchResult, Card cc) {
		ArrayList<Card> guess = new ArrayList<>();
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
				boolean hasEmblem = false;

				//If single-face card
				if(cc.card_faces == null) {
					for(Card gc: guess) {
						if (gc.name.equals(cc.name + " Emblem")) {
							hasEmblem = true;
							break;
						}
					}
				}
				//If card is double-faced
				else {
					for(CardFace cf: cc.card_faces) {
						for(Card gc: guess) {
							if(gc.name.equals(cf.name + " Emblem")) {
								hasEmblem = true;
								break;
							}
						}
					}
				}

				//If no emblem found, search for it
				if(!hasEmblem) {
					List<Card> g = SearchHelper.findTokensByName(sdm.tokens, cc.name+" Emblem", null, null, true);
					if(g.size() > 0) {
						SearchHelper.addTokenAndSources(searchResult, g.get(0), cc);
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
		ArrayList<Card>		results;
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

			return new TokenPrintingsResult(firstResult.getTokenSummaryTitle(face), results);
		}
				
		return new TokenPrintingsResult("NOT FOUND", null);
	}

	public static TokenByNameResultsContainer tokenNameSearchResults(String tokenlist) {
		ScryfallDataManager sdm = new ScryfallDataManager(false);
		ArrayList<TokenByNameResult> results = new ArrayList<>();

		ArrayList<String> notfound = new ArrayList<>();
		ArrayList<String> terms = new ArrayList<>(Arrays.asList(tokenlist.split("\n")));
		terms = search_string_remove_duplicates(terms, false);
				
		for(String t: terms) {
			String[] term = RegexHelper.extractPowerToughness(t);
			TokenByNameResult tbnr = new TokenByNameResult(t);
			
			ArrayList<Card> guesses = SearchHelper.findTokensByName(sdm.tokens, term[2].trim(), term[0], term[1], false, true);

			for (Card c : guesses) {
				boolean add = true;

				for (Card tok : tbnr.results) {
					if (MatchType.doesTokenPrintingMatch(tok, c, c.matching_face).match) {
						add = false;
					}
				}

				if (add) {
					int paren = c.display_name.indexOf("(");
					if (paren > -1) {
						c.display_name = c.display_name.substring(0, paren - 1);
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

		List<Card> containsCreate = new ArrayList<>();
    	
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
							if(rc.component.toLowerCase().equals("token")) {
								Card token = SearchHelper.findToken(sdm.tokens, rc.id);
								if(token != null) {
									goteem = true;
									SearchHelper.addTokenAndSources(searchResult, token, found);	
									if(OracleTextHelper.oracle_text_contains_create_multiple(found))
										containsCreate.add(found);
								}
							}
							else if(rc.component.toLowerCase().equals("meld_result")) {
								Card meld_card = SearchHelper.findCardByName(sdm.cards, true, rc.name, false);
								if(meld_card != null && OracleTextHelper.oracle_text_contains_create(meld_card)) {
									containsCreate.add(meld_card);
								}
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
		
    	searchResult.errors.removeIf(String::isEmpty);

    	if(searchResult.errors.size() == 0)
    		searchResult.errors = null;
    	
    	return searchResult;
    }
	
	public static List<Card> findDeadTokenCreators() {
		ScryfallDataManager sdm = new ScryfallDataManager(true);
		List<Card> 			dead_cards = new ArrayList<>();
		Set<String>			oracle_ids = new HashSet<>();
		Set<String>			negated_ids = new HashSet<>();

		//First remove all cards with officially-related tokens
		Iterator<Card> it = sdm.cards.listIterator();
		while(it.hasNext()) {
			Card c = it.next();
			if(c.hasScryfallRelatedToken()) {
				negated_ids.add(c.oracle_id);
			}
			
			if(!OracleTextHelper.oracle_text_contains_create(c)) {
				negated_ids.add(c.oracle_id);
			}
			
			if(negated_ids.contains(c.oracle_id)) {
				it.remove();
			}
		}
		
		for(Card c: sdm.cards) {
			if(findTipCards(sdm, c).size() == 0) {
				if(findManuallyCheckedTokens(sdm, c).size() == 0) {
					for(TokenGuess tg : SearchHelper.prepareTokenGuess(c)) {
						if(!tg.name.isEmpty()) {
							if(SearchHelper.findTokensByName(sdm.tokens, tg.name, tg.power, tg.toughness, true).size() == 0) {
								if(oracle_ids.add(c.oracle_id)) {
									c.setImages(false);
									dead_cards.add(c);
								}
							}
					    }
					}
				}
			}
		}
		Collections.sort(dead_cards);
		return dead_cards;
	}
}
