package utils;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public class DecimalFormatter {

    public static String format(double aDouble, int decimals) {
        StringBuilder pattern = new StringBuilder("#.");
        for (int i = 0; i < decimals; i++) {
            pattern.append("#");
        }
        DecimalFormat decimalFormat = new DecimalFormat(pattern.toString());
        decimalFormat.setRoundingMode(RoundingMode.CEILING);
        return decimalFormat.format(aDouble).replace(',', '.');
    }
}

