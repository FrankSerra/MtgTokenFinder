package HelperObjects;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexHelper {
	public static String[] extractPowerToughness(String text) {
		String power = null;
    	String toughness = null;
    	
    	//If it's a creature token, get the P/T declaration
    	Pattern ptOnly = Pattern.compile("[0-9xX\\*]+/[0-9xX\\*]+");
    	Matcher ptOnlyMatcher = ptOnly.matcher(text);
    	
    	Pattern pt = Pattern.compile("(?<=[0-9xX\\*]/[0-9xX\\*])(.*)");
    	Matcher ptMatcher = pt.matcher(text);
    	if(ptMatcher.find()) {
    		ptOnlyMatcher.find();
    		String stats = ptOnlyMatcher.group();
    		text = ptMatcher.group();
    		
    		power = stats.substring(0, stats.indexOf("/"));
    		toughness = stats.substring(stats.indexOf("/")+1, stats.length());
    	}
    	
    	return new String[] {power, toughness, text};
	}
	
	public static String extractName(String text) {
		Pattern clausePattern = Pattern.compile("(\\b[A-Z].*?\\b( |\\.))+(of |the )*(\\b[A-Z].*\\b)*");
        Matcher clauseMatcher = clausePattern.matcher(text);
        
        if(clauseMatcher.find()) { 	        	
        	return clauseMatcher.group().trim();
        }
        
        return null;
	}
}
