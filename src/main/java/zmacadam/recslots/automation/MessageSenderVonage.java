package zmacadam.recslots.automation;

import com.nexmo.client.NexmoClient;
import com.nexmo.client.sms.SmsSubmissionResponse;
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
public class MessageSenderVonage {
    public static final String API_KEY = "1d2bf9b2";
    public static final String API_SECRET = "gGoDW7YWZvbfBE8H";
    public static final String FROM_NUMBER = "15715097090";

    @Autowired
    private UserService userService;

    @Autowired
    private MessageRepository messageRepository;

    public void welcomeSMS(User user) {
        NexmoClient client = new NexmoClient.Builder()
                .apiKey(API_KEY)
                .apiSecret(API_SECRET)
                .build();
        String body = "Welcome " + user.getFirst() + " " + user.getLast() + "!" + "\n Your account is active and you will receive notifications based on your user preferences at slotsaver.com!";
        com.nexmo.client.sms.messages.TextMessage message = new com.nexmo.client.sms.messages.TextMessage(FROM_NUMBER, user.getPhoneNumber().substring(1), body);
        SmsSubmissionResponse response = client.getSmsClient().submitMessage(message);
    }

    public void sendSMS(String date, String slotType, String dayNumber, String body, int type) throws NoSuchFieldException, IllegalAccessException {

        List<User> users = userService.findByPaidTrue();
        NexmoClient client = new NexmoClient.Builder()
                .apiKey(API_KEY)
                .apiSecret(API_SECRET)
                .build();

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
            if (userPreferences.getField(dayNumber) && userPreferences.getField(slotType)) {
                com.nexmo.client.sms.messages.TextMessage message = new com.nexmo.client.sms.messages.TextMessage(FROM_NUMBER, user.getPhoneNumber().substring(1), body);
                SmsSubmissionResponse response = client.getSmsClient().submitMessage(message);
            }
        }
    }
}
