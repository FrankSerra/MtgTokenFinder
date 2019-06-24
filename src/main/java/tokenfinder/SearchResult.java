package tokenfinder;

import java.util.List;

public class SearchResult {
	public List<String> errors;
	public List<TokenResult> tokenResults;
	public List<ContainsCreateResult> containsCreate;
	
	public SearchResult(List<String> errors, List<TokenResult> tokenResults, List<ContainsCreateResult> containsCreate) {
		this.errors = errors;
		this.tokenResults = tokenResults;
		this.containsCreate = containsCreate;
	}
}
