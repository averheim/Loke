package services.spendperuserlast30days;

import java.util.Calendar;

public class Day {
    private Calendar date;
    private double dailyCost;

    public Day(Calendar date, double dailyCost) {
        this.date = date;
        this.dailyCost = dailyCost;
    }

    public Calendar getDate() {
        return date;
    }

    public double getDailyCost() {
        return dailyCost;
    }
}
