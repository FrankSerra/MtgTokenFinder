package tokenfinder;

import java.io.Serializable;
import java.util.List;

public class ContainsCreateResult implements Serializable, Comparable<ContainsCreateResult> {
	private static final long serialVersionUID = 1L;
	public Card card;
	public List<Card> guesses;
	public String error;
	
	public ContainsCreateResult(Card card, List<Card> guess, String error) {
		this.card = card;
		this.guesses = guess;
		this.error = error;
	}

	@Override
	public int compareTo(ContainsCreateResult o) {
		return this.card.compareTo(o.card);
	}
}
