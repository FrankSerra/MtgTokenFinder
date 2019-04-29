package tokenfinder;

import java.util.List;

public class SearchResult {
	public List<String> errors;
	public List<TokenResult> tokenResults;
	
	public SearchResult(List<String> errors, List<TokenResult> tokenResults) {
		this.errors = errors;
		this.tokenResults = tokenResults;
	}
}
