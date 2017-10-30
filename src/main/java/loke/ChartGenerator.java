package loke;

import loke.db.athena.AthenaClient;
import loke.model.Chart;
import loke.model.User;
import loke.services.ResourceStartedLastWeekDao;
import loke.services.Service;
import loke.services.SpendPerUserAndAccountDao;
import loke.services.SpendPerUserDao;

import java.util.ArrayList;
import java.util.List;

public class ChartGenerator {
    private List<Service> services;

    public ChartGenerator(AthenaClient athena, HtmlTableCreator htmlTableCreator, String userOwnerRegExp, double showAccountThreshold) {
        services = new ArrayList<>();
        services.add(new SpendPerUserDao(athena, htmlTableCreator, userOwnerRegExp));
        services.add(new SpendPerUserAndAccountDao(athena, htmlTableCreator, userOwnerRegExp, showAccountThreshold));
        services.add(new ResourceStartedLastWeekDao(athena, htmlTableCreator, userOwnerRegExp));
    }

    public List<User> generateChartsOrderedByUser() {
        return orderChartsByUser(getCharts());
    }

    private List<Chart> getCharts() {
        List<Chart> charts = new ArrayList<>();
        for (Service service : services) {
            charts.addAll(service.getCharts());
        }
        return charts;
    }

    private List<User> orderChartsByUser(List<Chart> charts) {
        List<User> users = new ArrayList<>();
        for (Chart chart : charts) {
            if (users.stream().noneMatch(user -> user.getUserName().equals(chart.getOwner()))) {
                users.add(new User(chart.getOwner()));
            }
            User user = findUser(users, chart.getOwner());
            if (user != null) {
                user.getCharts().add(chart);
            }
        }
        return users;
    }

    private User findUser(List<User> users, String owner) {
        for (User user : users) {
            if (user.getUserName().equals(owner)) {
                return user;
            }
        }
        return null;
        //test
    }

}
