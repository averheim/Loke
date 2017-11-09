package loke.utils;

import loke.service.Scale;

public class ScaleChecker {
    public static Scale checkScale(Double aDouble) {
        if (aDouble > 1000) return Scale.OVER_THOUSAND;
        if (aDouble > 100 || aDouble <= 1000) return Scale.OVER_HUNDRED;
        if (aDouble < 10) return Scale.UNDER_TEN;
        return Scale.UNDER_HUNDRED;
    }
}
