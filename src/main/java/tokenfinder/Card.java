package tokenfinder;

import java.util.ArrayList;

public class Card {
	public String oracle_id, id, oracle_text;
	public String name;
	public Image_URI image_uris;
	public ArrayList<Related_Card> all_parts;
	public ArrayList<CardFace> card_faces;
	public String scryfall_uri;
	public String display_name;
	public String power, toughness;
	public ArrayList<String> color_identity;
}
