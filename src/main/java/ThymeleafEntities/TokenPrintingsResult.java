package ThymeleafEntities;

import java.util.ArrayList;

import ScryfallData.Card;

public class TokenPrintingsResult {
	public final String cardname;
	public final ArrayList<Card> tokens;
	
	public TokenPrintingsResult(String _name, ArrayList<Card> _tokens) {
		this.cardname = _name;
		this.tokens   = _tokens;
	}

}
