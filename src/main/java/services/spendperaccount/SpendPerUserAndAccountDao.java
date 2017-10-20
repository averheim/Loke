package services.spendperaccount;

import db.athena.AthenaClient;
import db.athena.JdbcManager;
import model.Chart;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import services.Service;
import services.resourcestartedlastweek.ResourceStartedLastWeekDao;
import utils.DecimalFormatter;
import utils.HtmlTableCreator;
import utils.ResourceLoader;

import java.util.*;

public class SpendPerUserAndAccountDao implements Service {
    private AthenaClient athenaClient;
    private HtmlTableCreator htmlTableCreator;
    private static final String SQL_QUERY = ResourceLoader.getResource("sql/CostPerUserByProductAndAccount.sql");
    private static final Logger log = LogManager.getLogger(SpendPerUserAndAccountDao.class);

    public SpendPerUserAndAccountDao(AthenaClient athenaClient, HtmlTableCreator htmlTableCreator) {
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
            chart.setHtmlTable(generateHTMLTable(user));
            charts.add(chart);
            log.info(chart.getOwner() + "\n" + chart.getHtmlTable());
        }
        return charts;
    }

    public String generateHTMLTable(User user) {
        List<String> head = new ArrayList<>();
        head.addAll(Arrays.asList("Account", "Resource Name", "Cost"));

        double totalCost = 0;
        List<String> body = new ArrayList<>();
        for (Resource resource : user.getResources()) {
            body.add(resource.accountId);
            body.add(resource.productName);
            body.add(DecimalFormatter.format(resource.cost,2));
            totalCost += resource.cost;
        }

        String foot = "Total: " + DecimalFormatter.format(totalCost, 2);
        return htmlTableCreator.createTable(head, body, foot);
    }

    private Map<String, User> sendRequest() {
        Map<String, User> users = new HashMap<>();
        JdbcManager.QueryResult<Resource> queryResult = athenaClient.executeQuery(SQL_QUERY, Resource.class);
        for (Resource detailedResource : queryResult.getResultList()) {
            if (!users.containsKey(detailedResource.userOwner)) {
                users.put(detailedResource.userOwner, new User(detailedResource.userOwner));
            }
            users.get(detailedResource.userOwner).addResource(detailedResource);
        }
        return users;
    }

    private static class Resource {
        @JdbcManager.Column(value = "user_owner")
        private String userOwner;
        @JdbcManager.Column(value = "account_id")
        private String accountId;
        @JdbcManager.Column(value = "product_name")
        private String productName;
        @JdbcManager.Column(value = "resource_id")
        private String resourceId;
        @JdbcManager.Column(value = "start_date")
        private String startDate;
        @JdbcManager.Column(value = "cost")
        private double cost;
    }

    private class User {
        private String userOwner;
        private List<Resource> resources;

        public User(String userOwner) {
            this.userOwner = userOwner;
            this.resources = new ArrayList<>();
        }

        public String getUserOwner() {
            return userOwner;
        }

        public List<Resource> getResources() {
            return resources;
        }

        public void addResource(Resource resource) {
            resources.add(resource);
        }
    }
}
