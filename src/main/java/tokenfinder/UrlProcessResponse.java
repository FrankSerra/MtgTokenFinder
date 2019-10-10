package tokenfinder;

class UrlProcessResponse {
	final boolean okay;
	final String[] errors;
	final String cardlist;
	
	UrlProcessResponse(boolean okay, String[] errors, String cardlist) {
		this.okay = okay;
		this.errors = errors;
		this.cardlist = cardlist;
	}

}
