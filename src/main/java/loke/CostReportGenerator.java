package loke;

import loke.db.athena.AthenaClient;
import loke.model.Report;
import loke.model.User;
import loke.service.*;
import loke.service.dao.SpendPerUserDao;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class CostReportGenerator {
    private Logger log = LogManager.getLogger(CostReportGenerator.class);
    private List<Service> services;
    private List<User> admins;

    public CostReportGenerator(AthenaClient athena, HtmlTableCreator htmlTableCreator, String userOwnerRegExp, double showAccountThreshold) {
        SpendPerUserDao spendPerUserDao = new SpendPerUserDao(athena, userOwnerRegExp);

        services = new ArrayList<>();
        services.add(new AdminUserTotalReport(spendPerUserDao));
        services.add(new SpendPerUserReport(htmlTableCreator, spendPerUserDao));
        services.add(new SpendPerUserByAccountDao(athena, htmlTableCreator, userOwnerRegExp, showAccountThreshold));
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

    private List<Report> getCharts() {
        List<Report> reports = new ArrayList<>();
        for (Service service : services) {
            List<Report> reportList = service.getReports();
            addChartsToAdmins(service, reportList);
            reports.addAll(reportList);
        }
        return reports;
    }

    private void addChartsToAdmins(Service service, List<Report> reports) {
        if (service instanceof SpendPerUserByAccountDao || service instanceof AdminUserTotalReport) {
            for (Report report : reports) {
                log.info(report.getOwner());
                log.info(report.getHtmlURLs());
            }
            for (User admin : admins) {
                admin.getReports().addAll(reports);
            }
        }
    }

    private List<User> orderChartsByUser(List<Report> reports) {
        List<User> users = new ArrayList<>();
        for (Report report : reports) {
            if (users.stream().noneMatch(user -> user.getUserName().equals(report.getOwner()))) {
                users.add(new User(report.getOwner()));
            }
            User user = findUser(users, report.getOwner());
            if (user != null) {
                user.getReports().add(report);
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
    }
}
