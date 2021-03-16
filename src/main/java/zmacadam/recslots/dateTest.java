package zmacadam.recslots;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;

public class dateTest {
    public static void main(String[] args) throws ParseException {
        final LocalDate localDate = LocalDate.now().plusDays(2);
        DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd");
        Date date = df1.parse(localDate.toString());
        DateFormat df2 = new SimpleDateFormat("EEEEE MMM d, yyyy");
        String dateString = df2.format(date);
        System.out.println(dateString);

    }
}
