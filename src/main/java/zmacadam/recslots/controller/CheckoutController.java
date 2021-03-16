package zmacadam.recslots.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import zmacadam.recslots.model.ChargeRequest;

@Controller
public class CheckoutController {

    private String stripePublicKey = "pk_test_51IGA1ECWgKROeQIo1j9WvPEZ90nFLhOb1sOjhJBYng45xdI5NsppA6uHdpFlAJg3NHdJwbm1PWVVhRpJH88sNAnm00p52z1hr2";

    @RequestMapping("/checkout")
    public String checkout(Model model) {
        model.addAttribute("amount", 10 * 100);
        model.addAttribute("stripePublicKey", stripePublicKey);
        model.addAttribute("currency", ChargeRequest.Currency.USD);
        return "checkout";
    }
}
