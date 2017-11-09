package loke.utils;

import loke.service.Scale;

public class ScaleChecker {
    public static Scale checkScale(Double aDouble) {
        if (aDouble > 1000) return Scale.BETWEEN_1001_INFINITY;
        if (aDouble > 100 && aDouble <= 1000) return Scale.BETWEEN_101_1000;
        if (aDouble <= 10) return Scale.BETWEN_0_10;
        return Scale.BETWEEN_11_100;
    }
}
