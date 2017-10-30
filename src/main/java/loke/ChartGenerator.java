package loke;

import loke.db.athena.AthenaClient;
import loke.model.Chart;
import loke.model.User;
import loke.services.ResourceStartedLastWeekDao;
import loke.services.Service;
import loke.services.SpendPerUserAndAccountDao;
import loke.services.SpendPerUserDao;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class ChartGenerator {
    private Logger log = LogManager.getLogger(ChartGenerator.class);
    private List<Service> services;
    private List<User> admins;

    public ChartGenerator(AthenaClient athena, HtmlTableCreator htmlTableCreator, String userOwnerRegExp, double showAccountThreshold) {
        services = new ArrayList<>();
        services.add(new SpendPerUserDao(athena, htmlTableCreator, userOwnerRegExp));
        services.add(new SpendPerUserAndAccountDao(athena, htmlTableCreator, userOwnerRegExp, showAccountThreshold));
        services.add(new ResourceStartedLastWeekDao(athena, htmlTableCreator, userOwnerRegExp));
        admins = new ArrayList<>();
    }

    public List<User> generateChartsOrderedByUser() {
        List<User> users = orderChartsByUser(getCharts());
        users.addAll(admins);
        return users;
    }

    public void addAdmins(List<User> admins) {
        this.admins.addAll(admins);
    }

    private List<Chart> getCharts() {
        List<Chart> charts = new ArrayList<>();
        for (Service service : services) {
            List<Chart> serviceCharts = service.getCharts();
            addChartsToAdmins(service, serviceCharts);
            charts.addAll(serviceCharts);
        }
        return charts;
    }

    private void addChartsToAdmins(Service service, List<Chart> serviceCharts) {
        if (service instanceof SpendPerUserAndAccountDao) {
            log.info(serviceCharts.size());
            for (Chart serviceChart : serviceCharts) {
                log.info(serviceChart.getHtmlTables().size());
            }
            for (User admin : admins) {
                admin.getCharts().addAll(serviceCharts);
            }
        }
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
