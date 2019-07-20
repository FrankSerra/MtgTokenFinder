package HelperObjects;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ScryfallData.Card;
import ScryfallData.CardFace;

public class MatchType {
	public int card_face;
	public boolean match;
	private static final String conversionSymbols = "Xx*";
	private static final String conversionSymbolsRegex = "Xx\\*";
	private static final String conversionReplacement = "&";
	
	public MatchType(boolean _match, int _face) {
		this.match = _match;
		this.card_face = _face;
	}
	
	public static String getConversion(String s) {
		//Make X/X and */* interchangeable - temporarily change both to "&"
		if(s != null && conversionSymbols.contains(s))
			s = conversionReplacement;
		return s;
	}
	
	public static String getOracleTextConversion(String s, boolean already_regex) {
		Matcher match;
		String replaced;
		if(already_regex) {
			match = Pattern.compile("[" + conversionSymbolsRegex + "]\\\\/[" + conversionSymbolsRegex + "]").matcher(s);
		}
		else {
			match = Pattern.compile("[" + conversionSymbolsRegex + "]\\/[" + conversionSymbolsRegex + "]").matcher(s);
		}
		
		replaced = match.replaceAll(conversionReplacement + "/" + conversionReplacement);
		
		return replaced;
	}
	
	public static MatchType doesTokenMatch(Card c, String name, String power, String toughness) {
		return doesTokenMatch(c, name, power, toughness, false, false);
	}
	
	public static MatchType doesTokenMatch(Card c, String name, String power, String toughness, boolean ignoreCase, boolean allowContains) {
		power = getConversion(power);
		toughness = getConversion(toughness);
		
		if(ignoreCase) {
			name = name.toLowerCase();
		}
		
		//Do matching
		if(c.card_faces != null) {
			for(int face=0; face<c.card_faces.size(); face++) {
				CardFace cf = c.card_faces.get(face);
				String cardName = ignoreCase ? cf.name.toLowerCase() : cf.name;
				boolean matched = allowContains ? cardName.contains(name) : cardName.equals(name);
				
				if(matched) {
					if(power == null || (cf.power != null && getConversion(cf.power).equals(power))) {
						if(toughness == null || (cf.toughness != null && getConversion(cf.toughness).equals(toughness))) { 	
							return new MatchType(true, face);
						}
					}
		    	}
			}
		}
		else
		{
			String cardName = ignoreCase ? c.name.toLowerCase() : c.name;
			boolean matched = allowContains ? cardName.contains(name) : cardName.equals(name);
			if(matched) {
				if(power == null || (c.power != null && getConversion(c.power).equals(power))) {
					if(toughness == null || (c.toughness != null && getConversion(c.toughness).equals(toughness))) { 	
						return new MatchType(true, -1);
					}
				}
			}
		}
		
		return new MatchType(false, 2);
	}
	
	public static MatchType doesTokenPrintingMatch(Card c, Card original, int face) {
		//Set search parameters
    	String name 	 = original.getNameOnly(face);
    	String power 	 = original.getPower(face);
    	String toughness = original.getToughness(face);
    	String oracle    = original.getOracle(face);
    	
    	List<String> colors = original.getColors(face);
    	
    	return doesTokenPrintingMatch(c, name, power, toughness, colors, oracle);
	}
	
	public static MatchType doesTokenPrintingMatch(Card c, String name, String power, String toughness, List<String> colors, String oracle_text) {
		if(colors == null) {
			colors = new ArrayList<String>();
		}
		
		if(c.name.equals(name)) {
			if(power == null || (c.power != null && c.power.equals(power))) {
				if(toughness == null || (c.toughness != null && c.toughness.equals(toughness))) {
					if(oracle_text.equals(c.oracle_text)) {
						if(colors.equals(c.colors)) {
							return new MatchType(true, -1);
						}
					}
				}
			}
		}
		else if(c.card_faces != null) {
			for(int face=0; face<c.card_faces.size(); face++) {
				CardFace cf = c.card_faces.get(face);
				
				if(cf.name.equals(name)) {
					if(power == null || (cf.power != null && cf.power.equals(power))) {
						if(toughness == null || (cf.toughness != null && cf.toughness.equals(toughness))) { 	
							if(oracle_text.equals(cf.oracle_text)) {
								if(colors.equals(cf.colors)) {
									return new MatchType(true, face);
								}
							}
						}
					}
		    	}
			}
		}
		
		return new MatchType(false, 2);
	}
}