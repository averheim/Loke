package services.spendperaccount;

import db.athena.AthenaClient;
import db.athena.JdbcManager;
import model.Chart;
import services.Service;
import services.resourcestartedlastweek.ResourceStartedLastWeekDao;
import utils.HtmlTableCreator;
import utils.ResourceLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpendPerUserAndAccountDao implements Service {
    private AthenaClient athenaClient;
    private HtmlTableCreator htmlTableCreator;
    private static final String SQL_QUERY = ResourceLoader.getResource("sql/CostPerUserByProductAndAccount.sql");

    public SpendPerUserAndAccountDao(AthenaClient athenaClient, HtmlTableCreator htmlTableCreator) {
        this.athenaClient = athenaClient;
        this.htmlTableCreator = htmlTableCreator;
    }

    @Override
    public List<Chart> getCharts() {
        Map<String, User> users = sendRequest();
        return generateCharts(users);
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

    public static class Resource {
        @JdbcManager.Column(value = "user_owner")
        public String userOwner;
        @JdbcManager.Column(value = "account_id")
        public String accountId;
        @JdbcManager.Column(value = "product_name")
        public String resourceName;
        @JdbcManager.Column(value = "resource_id")
        public String resourceId;
        @JdbcManager.Column(value = "start_date")
        public String startDate;
        @JdbcManager.Column(value = "cost")
        public double cost;
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
