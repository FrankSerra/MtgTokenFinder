package tokenfinder;

import java.util.List;

public class SearchResult {
	public List<String> errors;
	public List<TokenResult> tokenResults;
	public List<Card> containsCreate;
	
	public SearchResult(List<String> errors, List<TokenResult> tokenResults, List<Card> containsCreate) {
		this.errors = errors;
		this.tokenResults = tokenResults;
		this.containsCreate = containsCreate;
	}
}
