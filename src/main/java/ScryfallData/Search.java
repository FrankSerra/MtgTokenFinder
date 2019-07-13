package ScryfallData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import HelperObjects.ImageSize;
import HelperObjects.OracleTextHelper;
import HelperObjects.SearchHelper;
import ThymeleafEntities.ContainsCreateResult;
import ThymeleafEntities.SearchResult;
import ThymeleafEntities.TokenGuess;
import ThymeleafEntities.TokenResult;

public class Search {
	private static ArrayList<String> search_string_to_list(String cardlist) {
    	//Search terms
		ArrayList<String> terms = new ArrayList<String>();
		terms.addAll(Arrays.asList(cardlist.split("\\s*\\r?\\n\\s*")));
		
		//Clear obvious list cuts
		terms.removeIf(p -> StringUtils.containsIgnoreCase(p, "sideboard"));
		terms.removeIf(p -> StringUtils.containsIgnoreCase(p, "maybeboard"));
		
		//Use a Set to remove duplicates, then put back into a sorted List
		HashSet<String> uniqueTerms = new HashSet<String> (terms);
		terms.clear();
		terms.addAll(uniqueTerms);
		
		//Prepare search terms
		terms.forEach(p -> p = SearchHelper.prepareSearchTerm(p));
		
		//Clear empties, then sort
		terms.removeIf(p -> p.isEmpty());
		Collections.sort(terms);		
		
		return terms;
	}
	
	public static SearchResult tokenResults(String cardlist, boolean matchExact, boolean includeSilver) {
		ScryfallDataManager sdm = new ScryfallDataManager(includeSilver);
    	List<String> errors = new ArrayList<String>();
    	List<TokenResult> results = new ArrayList<TokenResult>();
    	List<Card> containsCreate = new ArrayList<Card>();
    	List<ContainsCreateResult> ccResults = new ArrayList<ContainsCreateResult>();
    	List<String> full_list = new ArrayList<String>();
    	
    	try { 		
			for (String term: search_string_to_list(cardlist)) {
				Card found = SearchHelper.findCardByName(sdm.cards, matchExact, term, false);
				if(found == null) {
					found = SearchHelper.findCardByName(sdm.cards, matchExact, term, true);
				}
				
				if(found == null) {
					errors.add(term);
				}
				else {					
					full_list.add(term);
					if(found.all_parts != null) {			
						boolean goteem = false;
						for (Iterator<Related_Card> r = found.all_parts.iterator(); r.hasNext();) {
							Related_Card rc = r.next();
							Card token = SearchHelper.findToken(sdm.tokens, rc.id);
							if(token != null) {
								goteem = true;
								results = SearchHelper.addTokenAndSources(results, token, found);	
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
				}
				
				//Check for tip cards
				if(found != null) {
					if(OracleTextHelper.oracle_text_contains(found, "experience counter"))
						results = SearchHelper.addTokenAndSources(results, SearchHelper.findTipCard(sdm.tipcards, "Experience Counter"), found);
					
					if(OracleTextHelper.oracle_text_contains(found, "the monarch"))
						results = SearchHelper.addTokenAndSources(results, SearchHelper.findTipCard(sdm.tipcards, "The Monarch"), found);
					
					if(OracleTextHelper.oracle_text_contains(found, "{E}"))
						results = SearchHelper.addTokenAndSources(results, SearchHelper.findTipCard(sdm.tipcards, "Energy Reserve"), found);
					
					if(OracleTextHelper.oracle_text_contains(found, "the city's blessing"))
						results = SearchHelper.addTokenAndSources(results, SearchHelper.findTipCard(sdm.tipcards, "City's Blessing"), found);
					
					if(OracleTextHelper.oracle_text_contains(found, "infect ") || 
					   OracleTextHelper.oracle_text_contains(found, "infect.") || 
					   OracleTextHelper.oracle_text_contains(found, "poison counter"))
						results = SearchHelper.addTokenAndSources(results, SearchHelper.findTipCard(sdm.tipcards, "Poison Counter"), found);					
					
					if(OracleTextHelper.oracle_text_contains(found, "manifest th") || 
					   OracleTextHelper.oracle_text_contains(found, "manifest one") || 
					   OracleTextHelper.oracle_text_contains(found, "manifests "))
						results = SearchHelper.addTokenAndSources(results, SearchHelper.findTipCard(sdm.tipcards, "Manifest"), found);
					
					if(OracleTextHelper.oracle_text_contains(found, "megamorph ") || 
					   OracleTextHelper.oracle_text_contains_regex_multiline(found, "^Morph "))
						results = SearchHelper.addTokenAndSources(results, SearchHelper.findTipCard(sdm.tipcards, "Morph"), found);
				}
			}
			
			//Process containsCreate for token guesses	
	    	ArrayList<Card> copyToken = null;
	    	ArrayList<Card> amassToken = null;
	    	
			for(Card cc : containsCreate) {
				//Check for copy tokens
				if(OracleTextHelper.oracle_text_contains(cc, "that's a copy of") || OracleTextHelper.oracle_text_contains(cc, "that are copies of")) {
					if(copyToken == null)
						copyToken = SearchHelper.findTokensByName(sdm.tokens, "Copy", null, null, true);
					
					if(copyToken != null && copyToken.size() > 0)
						results = SearchHelper.addTokenAndSources(results, copyToken.get(0), cc);	
				}
				
				//Check for amass tokens
				if(OracleTextHelper.oracle_text_contains(cc, "amass ")) {
					if(amassToken == null)
						amassToken = SearchHelper.findTokensByName(sdm.tokens, "Zombie Army", "0", "0", true);
					
					if(amassToken != null && amassToken.size() > 0)
						results = SearchHelper.addTokenAndSources(results, amassToken.get(0), cc);
				}				
				
				ArrayList<Card> guess = new ArrayList<Card>();
				for(TokenGuess tg : SearchHelper.prepareTokenGuess(cc)) {
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
									SearchHelper.addTokenAndSources(results, g.get(0), cc);
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
										SearchHelper.addTokenAndSources(results, g.get(0), cc);
									}
								}
							}
						}
					}
					
					//Calculated image links
			    	cc.calculated_small  = ScryfallDataManager.getImageApiURL(cc, ImageSize.small, false);
			    	cc.calculated_normal = ScryfallDataManager.getImageApiURL(cc, ImageSize.normal, false);
			    	
			    	//Perform confidence matching
					boolean confidant = false;
					for(Card tokenCheck : guess) {
						if(SearchHelper.cardContainsTokenPhrase(cc, tokenCheck)) {
							confidant = true;
							SearchHelper.addTokenAndSources(results, tokenCheck, cc);
							break;
						}
					}

					if(!confidant)
						ccResults.add(new ContainsCreateResult(cc, guess, ""));
				}			    
			}
			
			//For each contains create object with no guesses attached:
			//Count number of times the word "create" appears (X)
			//Count number of times the card appears in the token sources list (Y)
			//If Y >= X, cut it from the list because all of the unknown tokens were confidence-matched
			Iterator<ContainsCreateResult> it = ccResults.iterator();
			while(it.hasNext()) {
				ContainsCreateResult ccr = it.next();
				int create_appears = OracleTextHelper.oracle_text_contains_create_count(ccr.card);
				int source_count = 0;
				for(TokenResult tr: results) {
					if(tr.sources.contains(ccr.card))
						source_count++;
				}
				
				if(source_count >= create_appears)
					it.remove();
			}
			
			errors.removeIf(p -> p.isEmpty());
    	}
    	catch(Exception e) {  		
    		errors.add("ERROR: " + e.getMessage());
    		e.printStackTrace();
    	}
    	
    	if(errors.size() == 0)
    		errors = null;
    	
    	return new SearchResult(full_list, errors, results, ccResults);
    }
}
