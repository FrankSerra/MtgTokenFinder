package ThymeleafEntities;

import java.util.ArrayList;

import ScryfallData.Card;

public class TokenPrintingsResult {
	public String cardname;
	public ArrayList<Card> tokens;
	
	public TokenPrintingsResult(String _name, ArrayList<Card> _tokens) {
		this.cardname = _name;
		this.tokens   = _tokens;
	}

}
