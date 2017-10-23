package services.resourcestartedlastweek;

import db.athena.AthenaClient;
import db.athena.JdbcManager;
import model.Chart;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import services.Service;
import utils.DecimalFormatter;
import utils.HtmlTableCreator;
import utils.ResourceLoader;

import java.util.*;

public class ResourceStartedLastWeekDao implements Service {
    private AthenaClient athenaClient;
    private HtmlTableCreator htmlTableCreator;
    private static final Logger log = LogManager.getLogger(ResourceStartedLastWeekDao.class);
    private static final String SQL_QUERY = ResourceLoader.getResource("sql/ResourceStartedLastWeek.sql");


    public ResourceStartedLastWeekDao(AthenaClient athenaClient, HtmlTableCreator htmlTableCreator) {
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
            Chart chart = new Chart(user.getUserOwner());
            chart.addHtmlTable(generateHTMLTable(user));
            charts.add(chart);
            log.info(chart.getOwner() + "\n" + chart.getHtmlTables());
        }
        return charts;
    }

    public String generateHTMLTable(User user) {
        List<String> head = new ArrayList<>();
        head.addAll(Arrays.asList("Account", "Product Name", "Resource Id", "Start Date", "Cost"));

        double totalCost = 0;
        List<String> body = new ArrayList<>();
        for (DetailedResource detailedResource : user.getResources()) {
            body.add(detailedResource.account);
            body.add(detailedResource.productName);
            body.add(detailedResource.resourceId);
            body.add(detailedResource.startDate);
            body.add(DecimalFormatter.format(detailedResource.cost,2));
            totalCost += detailedResource.cost;
        }

        String foot = "Total: " + DecimalFormatter.format(totalCost, 2);
        return htmlTableCreator.createTable(head, body, foot, null);
    }

    private Map<String, User> sendRequest() {
        Map<String, User> users = new HashMap<>();
        JdbcManager.QueryResult<DetailedResource> queryResult = athenaClient.executeQuery(SQL_QUERY, DetailedResource.class);
        for (DetailedResource detailedResource : queryResult.getResultList()) {
            if (!users.containsKey(detailedResource.userOwner)) {
                users.put(detailedResource.userOwner, new User(detailedResource.userOwner));
            }
            users.get(detailedResource.userOwner).addResource(detailedResource);
        }
        return users;
    }

    public static class DetailedResource {
        @JdbcManager.Column(value = "account_id")
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

    private class User {
        private String userOwner;
        private List<ResourceStartedLastWeekDao.DetailedResource> resources;

        public User(String userOwner) {
            this.userOwner = userOwner;
            this.resources = new ArrayList<>();
        }

        public String getUserOwner() {
            return userOwner;
        }

        public List<DetailedResource> getResources() {
            return resources;
        }

        public void addResource(DetailedResource resource) {
            resources.add(resource);
        }
    }
}
