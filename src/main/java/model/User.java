package model;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String userName;
    private List<Chart> charts;

    public User(String userName) {
        this.userName = userName;
        this.charts = new ArrayList<>();
    }

    public String getUserName() {
        return userName;
    }

    public List<Chart> getCharts() {
        return charts;
    }
}
