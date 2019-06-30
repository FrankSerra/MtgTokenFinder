package tokenfinder;

import java.util.ArrayList;
import java.util.List;

public class Card implements Comparable<Card> {
	public String oracle_id, id, oracle_text;
	public String name;
	public Image_URI image_uris;
	public ArrayList<Related_Card> all_parts;
	public ArrayList<CardFace> card_faces;
	public String scryfall_uri;
	public String power, toughness;
	public ArrayList<String> colors;
	public String set, collector_number;
	
	//These properties are for Thymeleaf and must be set manually
	public String display_name;
	public String display_oracle;
	public String calculated_small;
	public String calculated_normal;
	
	public String buildColorPhrase(List<String> color_identity) {
    	String disp = "";
    	if(color_identity.size() == 0) {
			disp += "Colorless ";
		}
		else {
			for(int idx=0; idx<color_identity.size(); idx++) {
				if(idx > 0)
					disp += "-";
				
				switch(color_identity.get(idx)) {
				case "W":
					disp += "White";
					break;
				case "U":
					disp += "Blue";
					break;
				case "B":
					disp += "Black";
					break;
				case "R":
					disp += "Red";
					break;
				case "G":
					disp += "Green";
					break;
				}
			}
		}
    	return disp;
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
