package tokenfinder;

public class UrlProcessResponse {
	boolean okay;
	String[] errors;
	String cardlist;
	
	public UrlProcessResponse(boolean okay, String[] errors, String cardlist) {
		this.okay = okay;
		this.errors = errors;
		this.cardlist = cardlist;
	}

}
