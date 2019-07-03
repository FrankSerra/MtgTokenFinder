package tokenfinder;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class URL_Processor {
	public static final String[] SupportedSites = new String[] {"Deckbox.org", "MTG Goldfish", "MTG Top 8", "MTG Vault", "TappedOut"};
	
	public static final SiteExclusion[] SiteExclusions = new SiteExclusion[] {
				 new SiteExclusion("Archidekt", "The site uses images to show deck content, so there are no card names to scrape."),
				 new SiteExclusion("Deckstats", "The site dynamically loads the deck content with JavaScript, so card names can't be scraped."),
				 new SiteExclusion("Moxfield",  "The site dynamically loads the deck content with JavaScript, so card names can't be scraped.")
				 };
	
	public static UrlProcessResponse ProcessURL(String deckboxurl) throws URISyntaxException {
		Map<String, Method> processorMap = new HashMap<String, Method>();
		
		try {
			processorMap.put("deckbox.org", URL_Processor.class.getMethod("fromDeckBox", String.class));
			processorMap.put("tappedout.net", URL_Processor.class.getMethod("fromTappedOut", String.class));
			processorMap.put("mtgvault.com", URL_Processor.class.getMethod("fromMtgVault", String.class));
			processorMap.put("mtggoldfish.com", URL_Processor.class.getMethod("fromMtgGoldfish", String.class));
			processorMap.put("mtgtop8.com", URL_Processor.class.getMethod("fromMtgTopEight", String.class));
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		
		URI parm = new URI(deckboxurl);
		
		String host = parm.getHost().toLowerCase();
		if(host.startsWith("www."))
			host = host.substring(4);
		
		if(processorMap.containsKey(host))
			try {
				return (UrlProcessResponse) processorMap.get(host).invoke(null, deckboxurl);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}
				
		return null;
	}
	
	//Individual processors
	public static UrlProcessResponse fromDeckBox(String deckboxurl) {	
		if(!deckboxurl.endsWith("/export"))
    		deckboxurl = deckboxurl + "/export";
    	
    	Document doc = null;
		try {
			doc = Jsoup.connect(deckboxurl).get();
		} catch (IOException e) {
			return new UrlProcessResponse(false, new String[] {"Error retrieving Deckbox URL."}, "");
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
    	
    	return new UrlProcessResponse(true, new String[] {"The TappedOut scraper cannot import the deck's commander, since it is an image. It will not appear below."}, list);
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
	
	public static UrlProcessResponse fromMtgTopEight(String mtgtop8) {
		Document doc = null;
		try {
			doc = Jsoup.connect(mtgtop8).get();
		} catch (IOException e) {
			return new UrlProcessResponse(false, new String[] {"Error retrieving MTG Top 8 URL.", e.getMessage()}, "");
		}
    	
		String list = "";
		for(Element e : doc.select("span.L14")) {			
			list += e.html() + "\n";
		}

		list = list.replace("<br>", "\n");
    	
    	return new UrlProcessResponse(true, null, list);
	}
	
}
