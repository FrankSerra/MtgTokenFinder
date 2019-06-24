package tokenfinder;

import java.io.Serializable;
import java.util.List;

public class ContainsCreateResult implements Serializable {
	private static final long serialVersionUID = 1L;
	public Card card;
	public List<Card> guesses;
	public String error;
	
	public ContainsCreateResult(Card card, List<Card> guess, String error) {
		this.card = card;
		this.guesses = guess;
		this.error = error;
	}
}
