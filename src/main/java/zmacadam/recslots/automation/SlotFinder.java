package zmacadam.recslots.automation;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import zmacadam.recslots.model.Date;
import zmacadam.recslots.model.User;
import zmacadam.recslots.model.UserPreferences;
import zmacadam.recslots.service.DateService;
import zmacadam.recslots.service.UserService;

import javax.annotation.PostConstruct;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

@Configuration
@EnableScheduling
public class SlotFinder {
    WebDriver driver;
    String[] messages = new String[]{
            "No new spots have opened up",
            "A new spot has opened up",
            "Spot(s) have been reserved",
            "A new day of slots have opened up"
    };

    List<String> weekdays = new ArrayList<>(Arrays.asList("6 - 9 AM",
            "10 AM - 2 PM",
            "3 - 7 PM",
            "8 - 11 PM",
            "8 - 10 PM"));

    List<String> weekends = new ArrayList<>(Arrays.asList(
        "10 AM - 2 PM",
        "3 - 6 PM",
        "7 - 10 PM"
    ));

    Map<String, String> weekNumbers = new HashMap<>(){{
        put("6 - 9 AM", "week1");
        put("10 AM - 2 PM", "week2");
        put("3 - 7 PM", "week3");
        put("8 - 11 PM", "week4");
        put("8 - 10 PM", "week4");
    }};

    Map<String, String> weekendNumbers = new HashMap<>(){{
       put("10 AM - 2 PM", "weekend1");
       put("3 - 6 PM", "weekend2");
       put("7 - 10 PM", "weekend3");
    }};

    boolean[] spans = new boolean[] {
            false, false, false, false
    };

    HashMap<String, String> days = new HashMap<>(){{
        put("MON", "Monday");
        put("TUE", "Tuesday");
        put("WED", "Wednesday");
        put("THU", "Thursday");
        put("FRI", "Friday");
        put("SAT", "Saturday");
        put("SUN", "Sunday");
    }};

    @Autowired
    DateService dateService;

    @Autowired
    MessageSenderTwilio messageSender;

    @Autowired
    UserService userService;

    @PostConstruct
    public void startup() {
        System.setProperty("webdriver.chrome.driver", "/usr/bin/chromedriver");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        driver = new ChromeDriver(options);
        try {
            driver.get("https://mycampusrec.tcu.edu/Account/Login?ReturnUrl=%2Fbooking%2Ffca496ac-99e5-4802-b785-7d7cf0664fdb");
            Thread.sleep(1000);
            WebElement TCULogin = driver.findElement(By.cssSelector(".loginOption[title='TCU Login']"));
            TCULogin.click();
            Thread.sleep(1000);
            WebElement userName = driver.findElement(By.id("okta-signin-username"));
            userName.sendKeys("zmacadam");
            WebElement password = driver.findElement(By.id("okta-signin-password"));
            password.sendKeys("GoFro123!");
            WebElement submit = driver.findElement(By.id("okta-signin-submit"));
            submit.click();
            Thread.sleep(2000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Scheduled(fixedDelay = 20000)
    public void getSlots() {
        try {
            List<WebElement> clearButtons = driver.findElements(By.className("innosoft-clear-button"));
            Thread.sleep(2000);
            WebElement dateSelector = clearButtons.get(0);
            Thread.sleep(2000);
            for (int i = 0; i < 3; i++) {
                dateSelector.click();
                Thread.sleep(1000);
                List<WebElement> dateButtons = driver.findElements(By.className("single-date-select-button"));
                WebElement dateButton = dateButtons.get(i);
                String dayName = dateButton.findElement(By.tagName("span")).getText();
                String stringDate = dateButton.getAttribute("data-date-text");
                Date date = dateService.findByDate(stringDate);
                if (date == null) {
                    date = new Date(stringDate);
                    date.initSlots();
                }
                dateButton.click();
                Thread.sleep(1000);
                WebElement apply = driver.findElement(By.xpath("//button[text()=\"Apply\"]"));
                apply.click();
                Thread.sleep(1000);
                List<WebElement> bookingSlots = driver.findElements(By.className("booking-slot-item"));
                Thread.sleep(2000);
                if (bookingSlots.size() == 0) {
                    date.initSlots();
                }
                for (WebElement slot : bookingSlots) {
                    String time = slot.findElement(By.tagName("strong")).getText();
                    int spanIndex;
                    String slotType;
                    if (dayName.equals("SAT") || dayName.equals("SUN")) {
                        spanIndex = weekends.indexOf(time);
                        slotType = weekendNumbers.get(time);
                        time = "W" + time;
                    } else {
                        spanIndex = weekdays.indexOf(time);
                        slotType = weekNumbers.get(time);
                    }
                    if (spanIndex == 4) {
                        spanIndex--;
                    }
                    int slotVal = date.getSlotValue(time);
                    spans[spanIndex] = true;
                    List<WebElement> spots = slot.findElements(By.tagName("span"));
                    int spot;
                    int prevSpot = slotVal;
                    String spotString;
                    String spotInt;
                    if (spots.size() > 1) {
                        spotString = spots.get(4).getText();
                        if (spotString.length() == 16) {
                            spot = 1;
                        } else {
                            if (spotString.substring(0, 2).equals("No")) {
                                spotInt = "0";
                            } else {
                                spotInt = spotString.substring(0, spotString.length() - 16);
                            }
                            spot = Integer.parseInt(spotInt);
                        }
                    } else {
                        spotString = spots.get(0).getText();
                        if (spotString.length() == 16) {
                            spot = 1;
                        } else {
                            if (spotString.substring(0, 2).equals("No")) {
                                spotInt = "0";
                            } else {
                                spotInt = spotString.substring(0, spotString.length() - 16);
                            }
                            spot = Integer.parseInt(spotInt);
                        }
                    }
                    int message = compareSpots(prevSpot, spot);
                    date.setSlot(time, spot);
                    int dayNumber = i + 1;
                    if (message == 1) {
                        if (time.substring(0,1).equals("W")) {
                            time = time.substring(1);
                        }
                        String body = messages[message] + " for " + days.get(dayName) + " " + date.getDate() + " at " + time + " meaning " + spot + " spot(s) are left" +
                                "\nhttps://mycampusrec.tcu.edu/Account/Login?ReturnUrl=%2Fbooking%2Ffca496ac-99e5-4802-b785-7d7cf0664fdb"
                                + "\nAlready have a slot for " + days.get(dayName) +"? Text \"pause\" to stop texts about " + days.get(dayName);
                        messageSender.sendSMS(date.getDate(), slotType, "day" + dayNumber, body);
                        date.setCount(time, spot);
                    }
                }
                for (int j = 0; j < spans.length; j++) {
                    if (!spans[j]) {
                        date.setSlot(weekdays.get(j), 0);
                    } else {
                        spans[j] = false;
                    }
                }
                dateService.saveDate(date);
                System.out.println(days.get(dayName) + " " + date.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            restart();
        }
    }


    @Scheduled(cron = "0 0 6 * * *", zone = "CST")
    public void newDay() throws ParseException {
        final LocalDate localDate = LocalDate.now().plusDays(2);
        DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd");
        java.util.Date date = df1.parse(localDate.toString());
        DateFormat df2 = new SimpleDateFormat("EEEEE MMM d, yyyy");
        String dateString = df2.format(date);
        String body = "A new day of slots is open! Don't forget to grab your slot for " + dateString + "!";
        List<User> users = userService.findByPaidTrue();
        for (User user : users) {
            UserPreferences userPreferences = user.getUserPreferences();
            userPreferences.setDay1(userPreferences.isDay2());
            userPreferences.setDay2(userPreferences.isDay3());
            userPreferences.setDay3(false);
            user.setCount(0);
            userService.updateUser(user);
            if (!userPreferences.isStop()) {
                messageSender.newDaySMS(user, body);
            }
        }
        restart();
    }

    public void restart() {
        driver.close();
        driver.quit();
        startup();
    }

    public int compareSpots(int prevSpot, int spot) {

        //no spots have opened up
        if (prevSpot == spot) {
            return 0;
        }

        //a new day of spots has opened up
        if (spot >= 300) {
            return 3;
        }

        //a new spot has opened up
        if (prevSpot == 0 && spot > prevSpot) {
            return 1;
        }

        //spots have been reserved
        if (prevSpot > spot && spot != 0) {
            return 2;
        }

        return 4;
    }
}
