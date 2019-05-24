package tokenfinder;

import java.util.ArrayList;

public class Card {
	public String oracle_id, id, oracle_text;
	public String name, type_line;
	public Image_URI image_uris;
	public ArrayList<Related_Card> all_parts;
	public ArrayList<CardFace> card_faces;
	String scryfall_uri;
	public String small_image;
	
	public String getOracle_id() {
		return oracle_id;
	}
	public void setOracle_id(String oracle_id) {
		this.oracle_id = oracle_id;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType_line() {
		return type_line;
	}
	public void setType_line(String type_line) {
		this.type_line = type_line;
	}
	public Image_URI getImage_uris() {
		return image_uris;
	}
	public void setImage_uris(Image_URI image_uris) {
		this.image_uris = image_uris;
	}
	public ArrayList<Related_Card> getAll_parts() {
		return all_parts;
	}
	public void setAll_parts(ArrayList<Related_Card> all_parts) {
		this.all_parts = all_parts;
	}
	public String getScryfall_uri() {
		return scryfall_uri;
	}
	public void setScryfall_uri(String scryfall_uri) {
		this.scryfall_uri = scryfall_uri;
	}
	public String getOracle_text() {
		return oracle_text;
	}
	public void setOracle_text(String oracle_text) {
		this.oracle_text = oracle_text;
	}
	public ArrayList<CardFace> getCard_faces() {
		return card_faces;
	}
	public void setCard_faces(ArrayList<CardFace> card_faces) {
		this.card_faces = card_faces;
	}
	public String getSmall_image() {
		return small_image;
	}
	public void setSmall_image(String small_image) {
		this.small_image = small_image;
	}
	
}
