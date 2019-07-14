package tokenfinder;

import java.net.URISyntaxException;
import java.util.Collections;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import ScryfallData.ScryfallDataManager;
import ScryfallData.Search;
import ThymeleafEntities.SearchResult;
import ThymeleafEntities.TokenPrintingsResult;
import ThymeleafEntities.TokenResult;

@Controller
public class CardController {

    @GetMapping({"/", "/tokens", "/fromurl"})
    public String cards(Model model) {
        return "redirect:/search";
    }
    
    @GetMapping("/search")
    public String search(Model model) {
    	model.addAttribute("sites", URL_Processor.SupportedSites);
    	model.addAttribute("exclusions", URL_Processor.SiteExclusions);
        return "search";
    }
    
    @GetMapping("/about")
    public String about(Model model) {
        return "about";
    }
    
    @GetMapping("/contact")
    public String contact(Model model) {
    	return "redirect:" + ScryfallDataManager.googleformURL();
    }
    
    @PostMapping("/tokens")
    public String tokens(@RequestParam(name="cardlist", required=true, defaultValue="") String cardlist, @RequestParam(name="matchExact", required=true, defaultValue="") String matchExact, @RequestParam(name="includeSilver", required=true, defaultValue="") String includeSilver, Model model) {
    	boolean _match  = matchExact.equals("on");
    	boolean	_silver = includeSilver.equals("on");
    	SearchResult sr = Search.tokenResults(cardlist, _match, _silver);

    	if(_silver) {
    		model.addAttribute("infonotes", new String[] { "Silver-bordered cards make use of Unicode symbols that each deck site handles differently. Results might not be found for every card." } );
    	}
    	
    	Collections.sort(sr.tokenResults);
    	for(TokenResult tr: sr.tokenResults) {
    		Collections.sort(tr.sources);
    	}
    	
    	Collections.sort(sr.containsCreate);
    	Collections.sort(sr.full_list);
    	
    	model.addAttribute("full_list", sr.full_list);
    	model.addAttribute("errors", sr.errors);
    	model.addAttribute("results", sr.tokenResults.isEmpty() ? null : sr.tokenResults);
    	model.addAttribute("contains_create", sr.containsCreate.isEmpty() ? null : sr.containsCreate);
    	
    	return "tokens";
    }
    
	@PostMapping("/fromurl")
    public String fromurl(@RequestParam(name="deckurl", required=true, defaultValue="") String deckurl, @RequestParam(name="includeSilver", required=true, defaultValue="") String includeSilver, Model model) {
    	UrlProcessResponse resp=null;
    	try {
    		if(deckurl.isEmpty())
    			throw new URISyntaxException("", "");
    		
    		resp = URL_Processor.ProcessURL(deckurl);			
		} catch (URISyntaxException e1) {
			model.addAttribute("errorlist", new String[] {"The URL entered was invalid."});
			return "error";
		}
    	
    	if(resp == null) {
    		return "unsupported_url";
    	}
    	else if(resp.okay) {
    		model.addAttribute("errorlist", resp.errors);
    		return tokens(resp.cardlist, "off", includeSilver, model);
    	}
    	else {
    		model.addAttribute("errorlist", resp.errors);
    		return "error";
    	}
    }
	
	@GetMapping("/viewprintings")
	public String printings(@RequestParam(name="oracleid", required=true, defaultValue="") String oracleid, @RequestParam(name="face", required=false, defaultValue="") String face, Model model) {
		int cardface;
		try {
			cardface = Integer.parseInt(face);
		}
		catch(Exception e) {
			cardface = -1;
		}
		
		TokenPrintingsResult tpr = Search.tokenPrintings(oracleid, cardface);
		
		model.addAttribute("results", tpr.tokens);
		model.addAttribute("cardname", tpr.cardname);
		
		return "printings";
	}
}