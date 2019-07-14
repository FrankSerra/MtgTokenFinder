package HelperObjects;

import java.util.List;

import ScryfallData.Card;
import ScryfallData.CardFace;

public class MatchType {
	public int card_face;
	public boolean match;
	
	public MatchType(boolean _match, int _face) {
		this.match = _match;
		this.card_face = _face;
	}
	
	public static MatchType doesTokenMatch(Card c, String name, String power, String toughness) {
		if(c.name.equals(name)) {
			if(power == null || (c.power != null && c.power.equals(power))) {
				if(toughness == null || (c.toughness != null && c.toughness.equals(toughness))) { 	
					return new MatchType(true, -1);
				}
			}
		}
		else if(c.card_faces != null) {
			for(int face=0; face<c.card_faces.size(); face++) {
				CardFace cf = c.card_faces.get(face);
				
				if(cf.name.equals(name)) {
					if(power == null || (cf.power != null && cf.power.equals(power))) {
						if(toughness == null || (cf.toughness != null && cf.toughness.equals(toughness))) { 	
							return new MatchType(true, face);
						}
					}
		    	}
			}
		}
		
		return new MatchType(false, 2);
	}
	
	public static MatchType doesTokenPrintingMatch(Card c, String name, String power, String toughness, List<String> colors, String oracle_text) {
		if(c.name.equals(name)) {
			if(power == null || (c.power != null && c.power.equals(power))) {
				if(toughness == null || (c.toughness != null && c.toughness.equals(toughness))) {
					if(c.colors.equals(colors)) {
						if(oracle_text.equals(c.oracle_text)) {
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
							if(cf.colors.equals(colors)) {
								if(oracle_text.equals(cf.oracle_text)) {
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