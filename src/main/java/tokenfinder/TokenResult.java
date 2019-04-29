package tokenfinder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TokenResult implements Serializable {
	private static final long serialVersionUID = 1L;
	public Card token;
	public List<Card> sources;
	public String error;
	
	public TokenResult(Card token, Card source, String error) {
		this.token = token;
		this.sources = new ArrayList<Card>();
		this.sources.add(source);
		this.error = error;
	}
}
