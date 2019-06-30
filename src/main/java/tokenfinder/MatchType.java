package tokenfinder;

public class MatchType {
	int card_face;
	boolean match;
	
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
}