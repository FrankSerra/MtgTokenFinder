package tokenfinder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import ThymeleafEntities.SiteExclusion;

class URL_Processor {
	static final String[] SupportedSites = new String[] {"Archidekt", "CubeTutor", "Deckbox.org",
																"Deckstats", "Moxfield", "MTG Goldfish", 
																"MTG Top 8", "MTG Vault", "StarCityGames", 
																"TappedOut"};
	
	static final SiteExclusion[] SiteExclusions = new SiteExclusion[] { };
	
	static UrlProcessResponse ProcessURL(String deckboxurl) throws URISyntaxException {
		Map<String, Method> processorMap = new HashMap<>();
		
		try {
			processorMap.put("archidekt.com", URL_Processor.class.getDeclaredMethod("fromArchidekt", String.class));
			processorMap.put("cubetutor.com", URL_Processor.class.getDeclaredMethod("fromCubeTutor", String.class));
			processorMap.put("deckbox.org", URL_Processor.class.getDeclaredMethod("fromDeckBox", String.class));
			processorMap.put("deckstats.net", URL_Processor.class.getDeclaredMethod("fromDeckstats", String.class));
			processorMap.put("moxfield.com", URL_Processor.class.getDeclaredMethod("fromMoxfield", String.class));
			processorMap.put("mtggoldfish.com", URL_Processor.class.getDeclaredMethod("fromMtgGoldfish", String.class));
			processorMap.put("mtgtop8.com", URL_Processor.class.getDeclaredMethod("fromMtgTopEight", String.class));
			processorMap.put("mtgvault.com", URL_Processor.class.getDeclaredMethod("fromMtgVault", String.class));
			processorMap.put("starcitygames.com", URL_Processor.class.getDeclaredMethod("fromSCG", String.class));
			processorMap.put("tappedout.net", URL_Processor.class.getDeclaredMethod("fromTappedOut", String.class));
			
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		
		URI parm = new URI(deckboxurl);
		
		String host = parm.getHost().toLowerCase();
		if(host.startsWith("www."))
			host = host.substring(4);
		
		if(processorMap.containsKey(host))
			try {
				Method m = processorMap.get(host);
				m.setAccessible(true);
				return (UrlProcessResponse) m.invoke(null, deckboxurl);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}
				
		return null;
	}
	
	//Individual processors
	private static UrlProcessResponse fromDeckBox(String deckboxurl) {
		if(!deckboxurl.endsWith("/export"))
    		deckboxurl = deckboxurl + "/export";
    	
    	Document doc;
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
	
	private static UrlProcessResponse fromTappedOut(String tappedout) {
		Document doc;
		try {
			doc = Jsoup.connect(tappedout).get();
		} catch (IOException e) {
			return new UrlProcessResponse(false, new String[] {"Error retrieving TappedOut URL. Is your deck publicly available?"}, "");
		}
    	
		StringBuilder list = new StringBuilder();
		for(Element e : doc.select("span.card:not(.card-token) > a.card-link")) {			
			list.append(e.html()).append("\n");
		}

		list = new StringBuilder(list.toString().replace("<br>", "\n"));
    	
    	return new UrlProcessResponse(true, new String[] {"The TappedOut scraper cannot import the deck's commander, since it is an image. It will not appear below."}, list.toString());
	}
	
	private static UrlProcessResponse fromMtgVault(String mtgvault) {
		Document doc;
		try {
			doc = Jsoup.connect(mtgvault).get();
		} catch (IOException e) {
			return new UrlProcessResponse(false, new String[] {"Error retrieving MTG Vault URL.", e.getMessage()}, "");
		}
    	
		StringBuilder list = new StringBuilder();
		for(Element e : doc.select("div.deck-card > span > a")) {			
			list.append(e.attr("title")).append("\n");
		}

		list = new StringBuilder(list.toString().replace("<br>", "\n"));
    	
    	return new UrlProcessResponse(true, null, list.toString());
	}
	
	private static UrlProcessResponse fromMtgGoldfish(String mtggoldfish) {
		Document doc;
		try {
			doc = Jsoup.connect(mtggoldfish).get();
		} catch (IOException e) {
			return new UrlProcessResponse(false, new String[] {"Error retrieving MTG Goldfish URL.", e.getMessage()}, "");
		}
    	
		StringBuilder list = new StringBuilder();
		for(Element e : doc.select("div#tab-paper table.deck-view-deck-table td.deck-col-card > a")) {			
			list.append(e.html()).append("\n");
		}

		list = new StringBuilder(list.toString().replace("<br>", "\n"));
    	
    	return new UrlProcessResponse(true, null, list.toString());
	}
	
	private static UrlProcessResponse fromMtgTopEight(String mtgtop8) {
		Document doc;
		try {
			doc = Jsoup.connect(mtgtop8).get();
		} catch (IOException e) {
			return new UrlProcessResponse(false, new String[] {"Error retrieving MTG Top 8 URL.", e.getMessage()}, "");
		}
    	
		StringBuilder list = new StringBuilder();
		for(Element e : doc.select("span.L14")) {			
			list.append(e.html()).append("\n");
		}

		list = new StringBuilder(list.toString().replace("<br>", "\n"));
    	
    	return new UrlProcessResponse(true, null, list.toString());
	}
	
	private static UrlProcessResponse fromMoxfield(String moxfield) {
		String file;
		try {
			URI parser = new URI(moxfield);
			String path = parser.getPath();
			if(path == null || path.isEmpty())
				throw new URISyntaxException("", "");
			
			path =  path.substring(path.lastIndexOf("/")+1);
			
		    InputStream inputStream = new URL("https://api.moxfield.com/v1/decks/all/" + path + "/download").openStream();
		 
		    StringBuilder textBuilder = new StringBuilder();
		    try (Reader reader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName(StandardCharsets.UTF_8.name())))) {
		        int c;
		        while ((c = reader.read()) != -1) {
		            textBuilder.append((char) c);
		        }
		        
		        file = textBuilder.toString();
		    }
		        
		} catch (IOException | URISyntaxException e) {
			return new UrlProcessResponse(false, new String[] {"Error retrieving Moxfield deck.", e.getMessage()}, "");
		}
    	
    	return new UrlProcessResponse(true, null, file);
	}

	private static UrlProcessResponse fromDeckstats(String deckstats) {
		String file;
		StringBuilder list = new StringBuilder();
		try {
			URI parser = new URI(deckstats);
			String path = parser.toString();
			
			if(path == null || path.isEmpty())
				throw new URISyntaxException("", "");
			
			path =  path.substring(path.lastIndexOf("?")+1);
			
		    InputStream inputStream = new URL(path + "/?export_txt=1").openStream();
		 
		    StringBuilder textBuilder = new StringBuilder();
		    try (Reader reader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName(StandardCharsets.UTF_8.name())))) {
		        int c;
		        while ((c = reader.read()) != -1) {
		            textBuilder.append((char) c);
		        }
		        
		        file = textBuilder.toString();
		        
		        Pattern removeCategory = Pattern.compile("[0-9]+ .*(\\r\\n|\\r|\\n)");
		        Matcher catMatches = removeCategory.matcher(file);
		        while (catMatches.find()) {
		        	list.append(catMatches.group());
		        }
		    }
		        
		} catch (IOException | URISyntaxException e) {
			return new UrlProcessResponse(false, new String[] {"Error retrieving Deckstats deck.", e.getMessage()}, "");
		}
    	
    	return new UrlProcessResponse(!Objects.equals(list.toString(), ""), null, list.toString());
	}
	
	private static UrlProcessResponse fromArchidekt(String archidekturl) {
    	String raw;
    	StringBuilder list = new StringBuilder();
		try {
			URI parser = new URI(archidekturl);
			String path = parser.getPath();
			
			if(path == null || path.isEmpty())
				throw new URISyntaxException("", "");
			
			path =  path.substring(path.lastIndexOf("/")+1);
			
			raw = Jsoup.connect("https://archidekt.com/api/decks/" + path + "/small/")
					.followRedirects(true)
					.ignoreContentType(true)
					.execute()
					.body();
			
		} catch (IOException | URISyntaxException e) {
			return new UrlProcessResponse(false, new String[] {"Error retrieving Archidekt deck."}, "");
		}
    	
		JsonElement root = new JsonParser().parse(raw);
		JsonArray cards = root.getAsJsonObject().get("cards").getAsJsonArray();
		for(JsonElement o: cards) {
			list.append(o.getAsJsonObject().get("card").getAsJsonObject().get("oracleCard").getAsJsonObject().get("name").getAsString()).append("\n");
		}
		
    	return new UrlProcessResponse(true, null, list.toString());
	}
	
	private static UrlProcessResponse fromSCG(String scg) {
		Document doc;
		try {
			doc = Jsoup.connect(scg).get();
		} catch (IOException e) {
			return new UrlProcessResponse(false, new String[] {"Error retrieving Star City Games URL.", e.getMessage()}, "");
		}
    	
		StringBuilder list = new StringBuilder();
		for(Element e : doc.select("div.deck_card_wrapper li > a")) {			
			list.append(e.html()).append("\n");
		}
    	
    	return new UrlProcessResponse(true, null, list.toString());
	}
	
	private static UrlProcessResponse fromCubeTutor(String scg) {
		Document doc;
		try {
			doc = Jsoup.connect(scg).get();
		} catch (IOException e) {
			return new UrlProcessResponse(false, new String[] {"Error retrieving CubeTutor URL. If you aren't using a /viewcube/ URL, please use it.", e.getMessage()}, "");
		}
    	
		StringBuilder list = new StringBuilder();
		for(Element e : doc.select("div#listContainer div.viewCubeColumn a")) {			
			list.append(e.html()).append("\n");
		}
		
		if(Objects.equals(list.toString(), ""))
			return new UrlProcessResponse(false, new String[] {"No cards found from CubeTutor link. If you aren't using the /viewcube/ List URL, please use it."}, "");
    	
    	return new UrlProcessResponse(true, null, list.toString());
	}
}
