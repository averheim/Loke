package loke.model;

import java.util.ArrayList;
import java.util.List;

public class Employee {
    private String userName;
    private List<Report> reports;

    public Employee(String userName) {
        this.userName = userName;
        this.reports = new ArrayList<>();
    }

    public String getUserName() {
        return userName;
    }

    public List<Report> getReports() {
        return reports;
    }

    public void addReport(Report report) {
        this.reports.add(report);
    }
}
