package ThymeleafEntities;

import java.util.ArrayList;
import java.util.List;

public class SearchResult {
	public List<String> errors, full_list;
	public List<TokenResult> tokenResults;
	public List<ContainsCreateResult> containsCreate;
	
	public SearchResult() {
		this.full_list = new ArrayList<String>();
		this.errors = new ArrayList<String>();
		this.tokenResults = new ArrayList<TokenResult>();
		this.containsCreate = new ArrayList<ContainsCreateResult>();
	}
	
	public SearchResult(List<String> full_list, List<String> errors, List<TokenResult> tokenResults, List<ContainsCreateResult> containsCreate) {
		this.full_list = full_list;
		this.errors = errors;
		this.tokenResults = tokenResults;
		this.containsCreate = containsCreate;
	}
}
