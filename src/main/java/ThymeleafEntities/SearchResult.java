package ThymeleafEntities;

import java.util.List;

public class SearchResult {
	public List<String> errors, full_list;
	public List<TokenResult> tokenResults;
	public List<ContainsCreateResult> containsCreate;
	
	public SearchResult(List<String> full_list, List<String> errors, List<TokenResult> tokenResults, List<ContainsCreateResult> containsCreate) {
		this.full_list = full_list;
		this.errors = errors;
		this.tokenResults = tokenResults;
		this.containsCreate = containsCreate;
	}
}
