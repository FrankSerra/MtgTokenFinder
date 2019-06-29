package tokenfinder;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class GoogleFormErrorController implements ErrorController  {
 
    @GetMapping("/error")
    public String handleError(Model model) {
        model.addAttribute("googleform", ScryfallDataManager.googleformURL());
        return "error";
    }
 
    @Override
    public String getErrorPath() {
        return "/error";
    }
}