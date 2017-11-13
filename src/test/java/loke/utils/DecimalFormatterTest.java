package loke.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DecimalFormatterTest {

    @Test
    public void format_withZeroDecimals_returnsTwoDecimals() throws Exception {
        String expected = "1.00";
        assertEquals(expected, DecimalFormatter.format(1, 0));
        assertEquals(expected, DecimalFormatter.format(1, 1));
        assertEquals(expected, DecimalFormatter.format(1, 2));
        assertEquals(expected, DecimalFormatter.format(1, 3));
    }

    @Test
    public void format_withDecimalsEqualToZero_returnsTwoDecimals() throws Exception {
        String expected = "1.00";
        assertEquals(expected, DecimalFormatter.format(1.00, 0));
        assertEquals(expected, DecimalFormatter.format(1.000, 1));
        assertEquals(expected, DecimalFormatter.format(1.000, 2));
        assertEquals(expected, DecimalFormatter.format(1.00000, 3));
    }

    @Test
    public void format_withDecimalOtherThanZero_returnsSpecifiedAmountsOfDecimals() throws Exception {
        double aDouble = 1.123456789;
        assertEquals("1.13", DecimalFormatter.format(aDouble, 2));
        assertEquals("1.124", DecimalFormatter.format(aDouble, 3));
        assertEquals("1.1235", DecimalFormatter.format(aDouble, 4));
        assertEquals("1.12346", DecimalFormatter.format(aDouble, 5));
        assertEquals("1.123457", DecimalFormatter.format(aDouble, 6));
    }

    @Test
    public void format_minimumAmountOfDecimalsIsAlwaysTwo() throws Exception {
        String expected = "1.13";
        double aDouble = 1.1234;
        assertEquals(expected, DecimalFormatter.format(aDouble, 0));
        assertEquals(expected, DecimalFormatter.format(aDouble, 1));
        assertEquals(expected, DecimalFormatter.format(aDouble, 2));
    }
}