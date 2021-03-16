package zmacadam.recslots.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import zmacadam.recslots.model.TextMessage;
import zmacadam.recslots.model.User;
import zmacadam.recslots.model.UserPreferences;
import zmacadam.recslots.repository.MessageRepository;
import zmacadam.recslots.service.UserService;

import java.util.List;

@RestController
public class SmsController {

    private static Logger logger = LoggerFactory.getLogger(SmsController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private MessageRepository messageRepository;

    @GetMapping(value = "/text")
    public String reply(@RequestParam("From") String from1,
                        @RequestParam("Body") String body1) throws NoSuchFieldException, IllegalAccessException {

        String from = from1.split(",")[0];
        String body = body1.split(",")[0];
        System.out.println(from);
        System.out.println(body);
        User user = userService.findUserByPhoneNumber(from);
        UserPreferences userPreferences = user.getUserPreferences();
        if (body.toLowerCase().trim().equals("pause")) {
            List<TextMessage> messageList = messageRepository.findAll();
            TextMessage cur = messageList.get(0);
            userPreferences.setField(cur.getDay(), false);
            userService.updateUser(user);
            return "Messages paused for " + cur.getDate() + "\n Need to resume? Change your preferences at slotsaver.com";
        }
        if (body.toLowerCase().trim().equals("stop")) {
            userPreferences.setStop(true);
            userService.updateUser(user);
            return "";
        }
        if (body.toLowerCase().trim().equals("start")) {
            userPreferences.setStop(false);
            userService.updateUser(user);
            return "";
        }
        return "Invalid command";
    }
}
