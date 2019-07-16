package ThymeleafEntities;

import java.util.ArrayList;

import ScryfallData.Card;

public class TokenByNameResult {
	public int idHash;
	public String searchTerm;
	public ArrayList<Card> results;
	
	public TokenByNameResult(String _term) {
		this.searchTerm = _term.trim();
		this.results = new ArrayList<Card>();
		
		this.idHash = this.searchTerm.hashCode();
	}
	
	public String SearchTerm() {
		return this.searchTerm;
	}
}
