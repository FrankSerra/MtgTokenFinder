package ThymeleafEntities;

import java.util.ArrayList;

public class TokenByNameResultsContainer {
	public ArrayList<TokenByNameResult> tbnrs;
	public ArrayList<String> 			terms;
	public ArrayList<String>			errors;
	
	public TokenByNameResultsContainer(ArrayList<TokenByNameResult> _tbnrs, ArrayList<String> _terms, ArrayList<String> _errors) {
		this.tbnrs  = _tbnrs;
		this.terms  = _terms;
		this.errors = _errors;
	}
}
