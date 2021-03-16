package zmacadam.recslots.controller;

import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;
import zmacadam.recslots.automation.MessageSenderTwilio;
import zmacadam.recslots.automation.MessageSenderVonage;
import zmacadam.recslots.model.ChargeRequest;
import zmacadam.recslots.model.User;
import zmacadam.recslots.service.StripeService;
import zmacadam.recslots.service.UserService;

@Controller
public class ChargeController {

    @Autowired
    StripeService paymentsService;

    @Autowired
    UserService userService;

    @Autowired
    MessageSenderTwilio messageSenderTwilio;

    @Autowired
    MessageSenderVonage messageSenderVonage;

    @PostMapping("/charge")
    public ModelAndView charge(ChargeRequest chargeRequest) throws StripeException {
        chargeRequest.setDescription("SlotSaver Signup");
        chargeRequest.setCurrency(ChargeRequest.Currency.USD);
        Charge charge = paymentsService.charge(chargeRequest);
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        user.setPaid(true);
        userService.updateUser(user);
        messageSenderTwilio.welcomeSMS(user);
//        messageSenderVonage.welcomeSMS(user);
        modelAndView.addObject("userName", "Welcome " + user.getFirst() + " " + user.getLast());
        modelAndView.addObject("user", user);
        modelAndView.addObject("preferences", user.getUserPreferences());
        modelAndView.addObject("id", charge.getId());
        modelAndView.addObject("status", charge.getStatus());
        modelAndView.addObject("chargeId", charge.getId());
        modelAndView.addObject("balance_transaction", charge.getBalanceTransaction());
        modelAndView.addObject("paymentSuccess", "Your payment was a success! Welcome to SlotSaver!");
        modelAndView.setViewName("home");
        return modelAndView;
    }

    @ExceptionHandler(StripeException.class)
    public String handleError(Model model, StripeException ex) {
        model.addAttribute("error", ex.getMessage());
        return "result";
    }
}
