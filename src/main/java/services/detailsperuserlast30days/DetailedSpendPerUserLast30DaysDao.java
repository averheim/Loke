package services.detailsperuserlast30days;

import db.athena.AthenaClient;
import db.athena.JdbcManager;
import model.Chart;
import org.apache.log4j.Logger;
import services.Service;
import services.spendperuserlast30days.Day;
import services.spendperuserlast30days.Resource;
import services.spendperuserlast30days.SpendPerUserLast30DaysDao;
import services.spendperuserlast30days.User;
import utils.DecimalFormatter;
import utils.HtmlTableCreator;
import utils.ResourceLoader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class DetailedSpendPerUserLast30DaysDao implements Service {
    private AthenaClient athenaClient;
    private HtmlTableCreator htmlTableCreator;
    private static final String SQL_QUERY = ResourceLoader.getResource("sql/UserResourceCostDetails.sql");
    private static final Logger log = Logger.getLogger(DetailedSpendPerUserLast30DaysDao.class);


    public DetailedSpendPerUserLast30DaysDao(AthenaClient athenaClient, HtmlTableCreator htmlTableCreator) {
        this.athenaClient = athenaClient;
        this.htmlTableCreator = htmlTableCreator;
    }

    @Override
    public List<Chart> getCharts() {
        Map<String, User> users = sendRequest();
        return generateCharts(users);
    }

    private List<Chart> generateCharts(Map<String, User> users) {
        List<Chart> charts = new ArrayList<>();
        for (User user : users.values()) {
            Chart chart = new Chart(user.userOwner);
            chart.setHtmlTable(generateHTMLTable(user));
            charts.add(chart);
            log.info(chart.getOwner() + "\n" + chart.getHtmlTable());
        }
        return charts;
    }

    public String generateHTMLTable(User user) {
        List<String> head = new ArrayList<>();
        head.addAll(Arrays.asList("Account", "Resource Name", "Resource Id", "Start Date", "Cost"));

        int totalCost = 0;
        List<String> body = new ArrayList<>();
        for (DetailedSpendPerUser detailedSpendPerUser : user.getResources()) {
            body.add(detailedSpendPerUser.account);
            body.add(detailedSpendPerUser.productName);
            body.add(detailedSpendPerUser.resourceId);
            body.add(detailedSpendPerUser.startDate);
            body.add(DecimalFormatter.format(detailedSpendPerUser.cost, 2));
            totalCost += detailedSpendPerUser.cost;
        }

        String foot = "Total: " + DecimalFormatter.format(totalCost, 2);
        return htmlTableCreator.createTable(head, body, foot);
    }

    private Map<String, User> sendRequest() {
        Map<String, User> users = new HashMap<>();
        JdbcManager.QueryResult<DetailedSpendPerUser> queryResult = athenaClient.executeQuery(SQL_QUERY, DetailedSpendPerUser.class);
        for (DetailedSpendPerUser detailedSpendPerUser : queryResult.getResultList()) {
            if (!users.containsKey(detailedSpendPerUser.userOwner)) {
                users.put(detailedSpendPerUser.userOwner, new User(detailedSpendPerUser.userOwner));
            }
            users.get(detailedSpendPerUser.userOwner).addResource(detailedSpendPerUser);
        }
        return users;
    }

    public static class DetailedSpendPerUser {
        @JdbcManager.Column(value = "linked_account_id")
        public String account;
        @JdbcManager.Column(value = "user_owner")
        public String userOwner;
        @JdbcManager.Column(value = "product_name")
        public String productName;
        @JdbcManager.Column(value = "resource_id")
        public String resourceId;
        @JdbcManager.Column(value = "start_date")
        public String startDate;
        @JdbcManager.Column(value = "cost")
        public double cost;
    }

    public class User {
        private String userOwner;
        private List<DetailedSpendPerUser> resources;

        public User(String userOwner) {
            this.userOwner = userOwner;
            this.resources = new ArrayList<>();
        }

        public String getUserOwner() {
            return userOwner;
        }

        public List<DetailedSpendPerUser> getResources() {
            return resources;
        }

        public void addResource(DetailedSpendPerUser resource) {
            resources.add(resource);
        }
    }
}
