package loke.utils;

import org.junit.Test;

import static loke.service.Scale.*;
import static loke.utils.ScaleChecker.checkScale;
import static org.junit.Assert.assertEquals;

public class ScaleCheckerTest {

    @Test
    public void checkScale_returnsAppropriateScale() throws Exception {
        assertEquals(BETWEN_0_10, checkScale(0.0));
        assertEquals(BETWEN_0_10, checkScale(9.0));
        assertEquals(BETWEN_0_10, checkScale(10.0));

        assertEquals(BETWEEN_11_100, checkScale(11.0));
        assertEquals(BETWEEN_11_100, checkScale(99.0));
        assertEquals(BETWEEN_11_100, checkScale(100.0));

        assertEquals(BETWEEN_101_1000, checkScale(101.0));
        assertEquals(BETWEEN_101_1000, checkScale(1000.0));

        assertEquals(BETWEEN_1001_INFINITY, checkScale(1001.0));
    }
}