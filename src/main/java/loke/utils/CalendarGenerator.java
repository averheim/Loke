package loke.utils;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CalendarGenerator {
    public static Clock clock = Clock.systemDefaultZone();

    public static List<Calendar> getDaysBack(int amount) {
        List<Calendar> days = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            Instant now = clock.instant();
            Instant minus = now.minus(i, ChronoUnit.DAYS);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(Date.from(minus));
            days.add(0, calendar);
        }
        return days;
    }
}