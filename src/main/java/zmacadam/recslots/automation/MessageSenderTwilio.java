package zmacadam.recslots.automation;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import zmacadam.recslots.model.TextMessage;
import zmacadam.recslots.model.User;
import zmacadam.recslots.model.UserPreferences;
import zmacadam.recslots.repository.MessageRepository;
import zmacadam.recslots.service.UserService;

import java.util.List;

@Component
public class MessageSenderTwilio {
    // Find your Account Sid and Token at twilio.com/user/account
    public static final String ACCOUNT_SID = "ACa7cab1eedc17cb1bf8534ab9e0990cc2";
    public static final String AUTH_TOKEN = "6801067c9a6149a04e0e468bd495b00b";
    public static final String FROM_NUMBER = "+13092502079";

    @Autowired
    private UserService userService;

    @Autowired
    private MessageRepository messageRepository;

    public void welcomeSMS(User user) {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
        String body = "Your account is active and you will receive notifications based on your user preferences at slotsaver.com!";
        Message message = Message.creator(new PhoneNumber(user.getPhoneNumber()),
                new PhoneNumber(FROM_NUMBER),
                body).create();
        System.out.println(message.getSid());
    }

    public void registerSMS(User user) {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
        String body = "Welcome " + user.getFirst() + "!" + "\nTo activate your account and receive texts, login and choose a payment option.";
        Message message = Message.creator(new PhoneNumber(user.getPhoneNumber()),
                new PhoneNumber(FROM_NUMBER),
                body).create();
        System.out.println(message.getSid());
    }

    public void newDaySMS(User user, String body) {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
        Message message = Message.creator(new PhoneNumber(user.getPhoneNumber()),
                new PhoneNumber(FROM_NUMBER),
                body).create();
        System.out.println(message.getSid());
    }

    public void sendSMS(String date, String slotType, String dayNumber, String body) throws NoSuchFieldException, IllegalAccessException {


        List<User> users = userService.findByPaidTrue();
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

        System.out.println(date + " " + slotType + " " + dayNumber);

        TextMessage textMessage;
        List<TextMessage> messages = messageRepository.findAll();
        if (messages.size() < 1) {
            textMessage = new TextMessage(date, slotType, dayNumber);
        } else {
            textMessage = messages.get(0);
            textMessage.setDate(date);
            textMessage.setSlot(slotType);
            textMessage.setDay(dayNumber);
        }
        messageRepository.save(textMessage);

        for (User user : users) {
            UserPreferences userPreferences = user.getUserPreferences();
            System.out.println("Day: " + userPreferences.getField(dayNumber) + " Time: " + userPreferences.getField(slotType));
            if ((userPreferences.getField(dayNumber) && userPreferences.getField(slotType)) && !userPreferences.isStop() && !dayNumber.equals("day3") && user.getCount() < 5) {
                user.setCount(user.getCount()+1);
                userService.updateUser(user);
                Message message = Message.creator(new PhoneNumber(user.getPhoneNumber()),
                        new PhoneNumber(FROM_NUMBER),
                        body).create();
                System.out.println(message.getSid());
            }
        }
    }
}
