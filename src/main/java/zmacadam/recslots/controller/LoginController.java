package zmacadam.recslots.controller;

import com.stripe.exception.StripeException;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import zmacadam.recslots.automation.MessageSenderTwilio;
import zmacadam.recslots.model.ChargeRequest;
import zmacadam.recslots.model.User;
import zmacadam.recslots.model.UserPreferences;
import zmacadam.recslots.repository.MessageRepository;
import zmacadam.recslots.service.UserService;

@Controller
@SessionAttributes("user")
public class LoginController {

    private String stripePublicKey = "pk_live_51IGA1ECWgKROeQIoJuQfrBUFvJG74kDwjG1aAgFxfAq6piyhHikpOu25av8wNIYtRu9q6HDKdQNbcaOSHCmGArWm00jaHu5rln";

    @Autowired
    private UserService userService;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private MessageSenderTwilio messageSender;


    @GetMapping(value={"/", "/login"})
    public ModelAndView login(){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("login");
        return modelAndView;
    }


    @GetMapping(value="/registration")
    public ModelAndView registration(){
        ModelAndView modelAndView = new ModelAndView();
        User user = new User();
        modelAndView.addObject("user", user);
        modelAndView.setViewName("registration");
        return modelAndView;
    }

    @PostMapping(value = "/registration")
    public ModelAndView createNewUser(User user, BindingResult bindingResult) throws StripeException {
        ModelAndView modelAndView = new ModelAndView();
        User userExists = userService.findUserByUserName(user.getUserName());
        if (userExists != null) {
            bindingResult
                    .rejectValue("userName", "error.user",
                            "There is already a user registered with the user name provided");
        }
        userExists = userService.findUserByPhoneNumber("+1" + user.getPhoneNumber().replaceAll("\\(", "")
                .replaceAll("\\)", "").replaceAll(" ", "").replaceAll("-", ""));
        if (userExists != null) {
            bindingResult
                    .rejectValue("phoneNumber", "error.user",
                            "There is already a user registered with the phone number provided");
        }
        if (bindingResult.hasErrors()) {
            modelAndView.setViewName("registration");
        } else {
            user.setPhoneNumber("+1" + user.getPhoneNumber().replaceAll("\\(", "")
                    .replaceAll("\\)", "").replaceAll(" ", "").replaceAll("-", ""));
            UserPreferences userPreferences = new UserPreferences().initPreferences();
            user.setUserPreferences(userPreferences);
            user.setPaid(false);
            userService.saveUser(user);
            userService.activateUser(user);
            messageSender.registerSMS(user);
            modelAndView.addObject("successMessage", "User registration success!");
            modelAndView.addObject("user", user);
            modelAndView.setViewName("login");
            //SEND TEXT VERIFICATION HERE
        }
        return modelAndView;
    }

    @GetMapping(value="/home")
    public ModelAndView home(){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName", "Welcome " + user.getFirst() + " " + user.getLast());
        modelAndView.addObject("user", user);
        modelAndView.addObject("preferences", user.getUserPreferences());
        modelAndView.addObject("amount", 20 * 100);
        modelAndView.addObject("stripePublicKey", stripePublicKey);
        modelAndView.addObject("currency", ChargeRequest.Currency.USD);
        modelAndView.setViewName("home");
        return modelAndView;
    }

    @PostMapping(value = "/updatePreferences")
    public ModelAndView updatePreferences(@RequestParam(value="day1", required = false) String day1,
                                          @RequestParam(value="day2", required = false) String day2,
                                          @RequestParam(value="day3", required = false) String day3,
                                          @RequestParam(value="week1", required = false) String week1,
                                          @RequestParam(value="week2", required = false) String week2,
                                          @RequestParam(value="week3", required = false) String week3,
                                          @RequestParam(value="week4", required = false) String week4,
                                          @RequestParam(value="weekend1", required = false) String weekend1,
                                          @RequestParam(value="weekend2", required = false) String weekend2,
                                          @RequestParam(value="weekend3", required = false) String weekend3) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("updateSuccess", "Your preferences have been updated!");
        modelAndView.addObject("userName", "Welcome " + user.getFirst() + " " + user.getLast());
        modelAndView.addObject("user", user);
        UserPreferences userPreferences = user.getUserPreferences();
        userPreferences.setDay1(day1 != null ? true : false);
        userPreferences.setDay2(day2 != null ? true : false);
        userPreferences.setDay3(day3 != null ? true : false);
        userPreferences.setWeek1(week1 != null ? true : false);
        userPreferences.setWeek2(week2 != null ? true : false);
        userPreferences.setWeek3(week3 != null ? true : false);
        userPreferences.setWeek4(week4 != null ? true : false);
        userPreferences.setWeekend1(weekend1 != null ? true : false);
        userPreferences.setWeekend2(weekend2 != null ? true : false);
        userPreferences.setWeekend3(weekend3 != null ? true : false);
        userService.updateUser(user);
        modelAndView.addObject("preferences", userPreferences);
        modelAndView.setViewName("home");
        return modelAndView;
    }



}
