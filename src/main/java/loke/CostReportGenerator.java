package loke;

import loke.db.athena.AthenaClient;
import loke.model.*;
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
        services.add(new TotalSpendPerUserReport(spendPerUserDao));
        services.add(new SpendPerUserByResourceReport(htmlTableCreator, spendPerUserDao));
        services.add(new SpendPerUserByAccountDao(athena, htmlTableCreator, userOwnerRegExp, showAccountThreshold));
        services.add(new ResourceStartedLastWeekDao(athena, htmlTableCreator, userOwnerRegExp));
        admins = new ArrayList<>();
    }

    public List<User> generateReportsOrderedByUser() {
        List<User> users = orderChartsByUser(getReports());
        addReportsToAdmin(users);
        users.addAll(admins);
        return users;
    }

    public void addAdmins(List<AdminUser> admins) {
        this.admins.addAll(admins);
    }

    private List<Report> getReports() {
        List<Report> reports = new ArrayList<>();
        for (Service service : services) {
            List<Report> reportList = service.getReports();
            reports.addAll(reportList);
        }
        return reports;
    }

    private void addReportsToAdmin(List<User> users) {
        List<Report> costReports = new ArrayList<>();
        for (User user : users) {
            List<Report> reports = user.getReports();
            for (Report report : reports) {
                if (report instanceof TotalReport) costReports.add(report);
                else if(report instanceof TotalPerAccountReport) costReports.add(report);
            }
        }
        for (User admin : admins) {
            admin.addReports(costReports);
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
