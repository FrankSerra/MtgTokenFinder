package HelperObjects;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import ScryfallData.Card;
import ScryfallData.CardFace;

public class OracleTextHelper {
	public static boolean oracle_text_contains_create(Card c) {
    	if(c.oracle_text != null) {
    		return (StringUtils.contains(c.oracle_text, " create") || StringUtils.contains(c.oracle_text, "Create "));
    	}
    	else if(c.card_faces != null && c.card_faces.size() > 0) {
    		for (CardFace face : c.card_faces) {
				if(StringUtils.contains(face.oracle_text, " create") || StringUtils.contains(face.oracle_text, "Create "))
					return true;
			}
    	}
    	
    	return false;
    }
	
	public static boolean oracle_text_contains_create_multiple(Card c) {
		return oracle_text_contains_create_count(c) > 1;
	}
	
	public static int oracle_text_contains_create_count(Card c) {
    	if(c.oracle_text != null) {
    		return (StringUtils.countMatches(c.oracle_text, "Create") + StringUtils.countMatches(c.oracle_text, " create") - StringUtils.countMatches(c.oracle_text, " created"));
    	}
    	else if(c.card_faces != null && c.card_faces.size() == 2) {
    		return Math.max(
    				StringUtils.countMatches(c.card_faces.get(0).oracle_text, "Create") + StringUtils.countMatches(c.card_faces.get(0).oracle_text, " create") - StringUtils.countMatches(c.card_faces.get(0).oracle_text, " created"),
    				StringUtils.countMatches(c.card_faces.get(1).oracle_text, "Create") + StringUtils.countMatches(c.card_faces.get(1).oracle_text, " create") - StringUtils.countMatches(c.card_faces.get(1).oracle_text, " created")
    				);
    	}
    	else if(c.card_faces != null && c.card_faces.size() == 1) {
    		return StringUtils.countMatches(c.card_faces.get(0).oracle_text, "Create") + StringUtils.countMatches(c.card_faces.get(0).oracle_text, " create") - StringUtils.countMatches(c.card_faces.get(0).oracle_text, " created");
    	}
    	
    	return 0;
    }
	
	public static boolean oracle_text_contains(Card c, String text) {
    	if(c.oracle_text != null) {
    		return (StringUtils.containsIgnoreCase(c.oracle_text, text));
    	}
    	else if(c.card_faces != null && c.card_faces.size() > 0) {
    		for (CardFace face : c.card_faces) {
				if(StringUtils.containsIgnoreCase(face.oracle_text, text))
					return true;
			}
    	}
    	
    	return false;
    }
	
	static boolean oracle_text_contains_regex(Card c, String text) {
		if(c.oracle_text != null) {
    		return MatchType.getOracleTextConversion(c.oracle_text.replace("\r", "").replace("\n", "").toLowerCase(), false).matches(text.toLowerCase());
    	}
    	else if(c.card_faces != null && c.card_faces.size() > 0) {
    		for (CardFace face : c.card_faces) {
				if(MatchType.getOracleTextConversion(face.oracle_text.replace("\r", "").replace("\n", "").toLowerCase(), false).matches(text.toLowerCase()))
					return true;
			}
    	}
    	
    	return false;
    }

    public static void oracle_text_remove(Card c, String text) {
		String line;
		if(c.oracle_text != null) {
			c.oracle_text = c.oracle_text.replaceAll(text, "");

		}
		else if(c.card_faces != null && c.card_faces.size() > 0) {
			for (CardFace face : c.card_faces) {
				face.oracle_text = face.oracle_text.replaceAll(text, "");
			}
		}
	}
	
	public static boolean oracle_text_contains_regex_multiline(Card c, String text) {
		Pattern pattern = Pattern.compile(text, Pattern.MULTILINE);
		
		if(c.oracle_text != null) {
    		return pattern.matcher(c.oracle_text).find();
    	}
    	else if(c.card_faces != null && c.card_faces.size() > 0) {
    		for (CardFace face : c.card_faces) {
				if(pattern.matcher(face.oracle_text).find())
					return true;
			}
    	}
    	
    	return false;
    }
}
