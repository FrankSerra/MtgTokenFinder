package tokenfinder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Card implements Comparable<Card> {
	public String oracle_id, id, oracle_text, tcgplayer_id;
	public String name;
	public Image_URI image_uris;
	public ArrayList<Related_Card> all_parts;
	public ArrayList<CardFace> card_faces;
	public String scryfall_uri;
	public String power, toughness;
	public ArrayList<String> colors;
	public String set, collector_number, type_line;
	
	//These properties are for Thymeleaf and must be set manually
	public String display_name;
	public String display_oracle;
	public String calculated_small;
	public String calculated_normal;
	
	public String buildColorPhrase(List<String> color_identity) {
    	String disp = "";
    	if(color_identity.size() == 0) {
			disp += "Colorless";
		}
		else {
			for(int idx=0; idx<color_identity.size(); idx++) {
				if(idx > 0)
					disp += "-";
				
				disp += SearchHelper.letterToWord(color_identity.get(idx));
				
			}
		}
    	return disp;
    }
	
	public String buildOracleColorPhrase(List<String> color_identity) {
		//Unfortunately, the words are always in WUBRG order, so we need to do manual checks
    	if(color_identity == null || color_identity.size() == 0) {
			return "colorless";
		}
		
    	final String ORDER= "WUBRG";

    	color_identity.sort(new Comparator<String>() {
    	    @Override
    	    public int compare(String o1, String o2) {
    	       return ORDER.indexOf(o1.toUpperCase()) -  ORDER.indexOf(o2.toUpperCase()) ;
    	    }
    	});
    	
    	if(color_identity.size() == 1) {
			return SearchHelper.letterToWord(color_identity.get(0)).toLowerCase();
		}
		else if(color_identity.size() == 2) {
			return SearchHelper.letterToWord(color_identity.get(0)).toLowerCase() + " and " + SearchHelper.letterToWord(color_identity.get(1)).toLowerCase();
		}
		else if(color_identity.size() == 3) {
			return SearchHelper.letterToWord(color_identity.get(0)).toLowerCase() + ", " + SearchHelper.letterToWord(color_identity.get(1)).toLowerCase() + ", and " + SearchHelper.letterToWord(color_identity.get(2)).toLowerCase();
		}
		else {
			return this.buildColorPhrase(color_identity).toLowerCase();
		}
    }
    
    public String getPower(int face) {
    	switch(face) {
    	case -1:
    		return this.power;
    	default:
    		return this.card_faces.get(face).power;
    	}
    }
    
    public String getToughness(int face) {
    	switch(face) {
    	case -1:
    		return this.toughness;
    	default:
    		return this.card_faces.get(face).toughness;
    	}
    }
    
    public String getName(int face) {
    	switch(face) {
    	case -1:
    		return this.name;
    	default:
    		return this.card_faces.get(face).name + " (DFC with: " + this.card_faces.get((face*-1)+1).name + ")";
    	}
    }
    
    public String getTypes(int face) {
    	//won't return the words "token" or "creature"
    	String types = "";
    	switch(face) {
    	case -1:
    		types = this.type_line;
    		break;
    	default:
    		types = this.card_faces.get(face).type_line;
    		break;
    	}
    	    	
    	Pattern pattern = Pattern.compile(".*(?= [^\\w])");
    	Matcher match = pattern.matcher(types);
    	
    	if(match.find()) {
    		types = match.group();
    		return types.replace("Token", "").trim().replaceAll(" +", " ");
    	}
    	
    	return "";
    }
    
    public String getOracle(int face) {
    	switch(face) {
    	case -1:
    		return this.oracle_text;
    	default:
    		return this.card_faces.get(face).oracle_text;
    	}
    }
    
    public List<String> getColors(int face) {
    	switch(face) {
    	case -1:
    		return this.colors;
    	default:
    		return this.card_faces.get(face).colors;
    	}
    }
    
    public void trimCardFace(int face) {
    	if(face > 1)
    		return;
    	
    	this.card_faces.remove(face);
    }

	@Override
	public int compareTo(Card o) {
		int name = this.name.compareTo(o.name);
		if(name == 0) {
			int power = this.power.compareTo(o.power);
			if(power == 0) {
				int tough = this.toughness.compareTo(o.toughness);
				if(tough == 0) {
					int oracle = this.oracle_text.compareTo(o.oracle_text);
					return oracle;
				}
				else return tough;
			}
			else
				return power;
		}
		
		return name;
	}

}
