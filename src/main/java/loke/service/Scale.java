package loke.service;

import java.util.Arrays;
import java.util.List;

public enum Scale {
    UNDER_TEN("USD", 0.1, Arrays.asList(0,1,2,3,4,5,6,7,8,9,10)),
    UNDER_HUNDRED("USD", 1, Arrays.asList(0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100)),
    OVER_HUNDRED("hundred USD", 10, Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10)),
    OVER_THOUSAND("thousand USD", 100, Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10));

    private final String name;
    private final double divideBy;
    private final List<Integer> yAxisLabels;

    Scale(String name, double divideBy, List<Integer> yAxisLabels) {
        this.name = name;
        this.divideBy = divideBy;
        this.yAxisLabels = yAxisLabels;
    }

    public String getSuffix() {
        return name;
    }

    public double getDivideBy() {
        return divideBy;
    }

    public List<Integer> getyAxisLabels() {return yAxisLabels;}
}
