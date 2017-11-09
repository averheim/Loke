package loke.utils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.Instant;
import java.util.Calendar;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CalendarGeneratorTest {
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private Clock clock;

    @Before
    public void setUp() throws Exception {
        clock = mock(Clock.class);
        CalendarGenerator.clock = clock;
        when(clock.instant()).thenReturn(Instant.parse("2017-09-30T00:00:00Z"));
    }

    @Test
    public void getDaysBack() throws Exception {
        List<Calendar> daysBack = CalendarGenerator.getDaysBack(3);

        Assert.assertEquals("2017-09-28", getFormattedDate(daysBack.get(0)));
        Assert.assertEquals("2017-09-29", getFormattedDate(daysBack.get(1)));
        Assert.assertEquals("2017-09-30", getFormattedDate(daysBack.get(2)));
        Assert.assertEquals(3, daysBack.size());

    }

    private String getFormattedDate(Calendar calendar) {
        return dateFormat.format(calendar.getTime());
    }

}