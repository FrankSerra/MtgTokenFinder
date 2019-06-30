package tokenfinder;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class URL_Processor {
	public static final String[] SupportedSites = new String[] {"Deckbox.org", "MTGGoldfish.com", "MTGVault.com", "TappedOut.net"};
	
	/*
	 * Unsupported sites and why
	 * Archidekt - the site only uses images, there are no card names to scrape without logging in and editing the deck.
	*/
	
	public static UrlProcessResponse fromDeckBox(String deckboxurl) {	
		if(!deckboxurl.endsWith("/export"))
    		deckboxurl = deckboxurl + "/export";
    	
    	Document doc = null;
		try {
			doc = Jsoup.connect(deckboxurl).get();
		} catch (IOException e) {
			return new UrlProcessResponse(false, new String[] {"Error retrieving URL."}, "");
		}
    	
		Element cards = doc.select("body").first();
		doc.select("p").remove();
    	String list = cards.html();
    	list = list.replace("<br>", "\n");
    	
    	return new UrlProcessResponse(true, null, list);
	}
	
	public static UrlProcessResponse fromTappedOut(String tappedout) {
		Document doc = null;
		try {
			doc = Jsoup.connect(tappedout).get();
		} catch (IOException e) {
			return new UrlProcessResponse(false, new String[] {"Error retrieving TappedOut URL. Is your deck publicly available?"}, "");
		}
    	
		String list = "";
		for(Element e : doc.select("span.card:not(.card-token) > a.card-link")) {			
			list += e.html() + "\n";
		}

		list = list.replace("<br>", "\n");
    	
    	return new UrlProcessResponse(true, null, list);
	}
	
	public static UrlProcessResponse fromMtgVault(String mtgvault) {
		Document doc = null;
		try {
			doc = Jsoup.connect(mtgvault).get();
		} catch (IOException e) {
			return new UrlProcessResponse(false, new String[] {"Error retrieving MTG Vault URL.", e.getMessage()}, "");
		}
    	
		String list = "";
		for(Element e : doc.select("div.deck-card > span > a")) {			
			list += e.attr("title") + "\n";
		}

		list = list.replace("<br>", "\n");
    	
    	return new UrlProcessResponse(true, null, list);
	}
	
	public static UrlProcessResponse fromMtgGoldfish(String mtggoldfish) {
		Document doc = null;
		try {
			doc = Jsoup.connect(mtggoldfish).get();
		} catch (IOException e) {
			return new UrlProcessResponse(false, new String[] {"Error retrieving MTG Goldfish URL.", e.getMessage()}, "");
		}
    	
		String list = "";
		for(Element e : doc.select("div#tab-paper table.deck-view-deck-table td.deck-col-card > a")) {			
			list += e.html() + "\n";
		}

		list = list.replace("<br>", "\n");
    	
    	return new UrlProcessResponse(true, null, list);
	}
}
