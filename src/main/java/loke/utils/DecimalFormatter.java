package loke.utils;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

public class DecimalFormatter {

    public static String format(double aDouble, int decimals) {
        StringBuilder pattern = new StringBuilder("###,###.");
        for (int i = 0; i < decimals; i++) {
            pattern.append("#");
        }

        DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
        formatter.applyPattern(pattern.toString());
        formatter.setRoundingMode(RoundingMode.CEILING);
        DecimalFormatSymbols symbols = formatter.getDecimalFormatSymbols();

        symbols.setGroupingSeparator(' ');
        formatter.setDecimalFormatSymbols(symbols);

        return formatter.format(aDouble);
    }
}
