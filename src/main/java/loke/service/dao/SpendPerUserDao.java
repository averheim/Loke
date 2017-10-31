package loke.service.dao;

import loke.db.athena.AthenaClient;
import loke.db.athena.JdbcManager;
import loke.utils.ResourceLoader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class SpendPerUserDao {
    private static final String SQL_QUERY = ResourceLoader.getResource("sql/SpendPerUser.sql");
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private AthenaClient athenaClient;
    private String userOwnerRegexp;


    public SpendPerUserDao(AthenaClient athenaClient, String userOwnerRegexp) {
        this.athenaClient = athenaClient;
        this.userOwnerRegexp = userOwnerRegexp;
    }


    public Map<String, User> getUsers() {
        Map<String, User> users = new HashMap<>();
        JdbcManager.QueryResult<SpendPerUser> queryResult = athenaClient.executeQuery(SQL_QUERY, SpendPerUser.class);
        for (SpendPerUser spendPerUser : queryResult.getResultList()) {
            if (!spendPerUser.userOwner.matches(userOwnerRegexp)) {
                continue;
            }

            String userOwner = spendPerUser.userOwner;
            String startDate = spendPerUser.startDate;
            String productName = spendPerUser.productName;

            if (!users.containsKey(userOwner)) {
                users.put(userOwner, new User(userOwner));
            }
            if (!users.get(userOwner).getResources().containsKey(productName)) {
                users.get(userOwner).addResource(new Resource(productName));
            }

            Calendar date = Calendar.getInstance();
            try {
                date.setTime(dateFormat.parse(startDate));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Day day = new Day(date, spendPerUser.cost);
            users.get(userOwner).getResources().get(productName).addDay(dateFormat.format(day.getDate().getTime()), day);
        }
        return users;
    }

    public static class SpendPerUser {
        @JdbcManager.Column(value = "user_owner")
        public String userOwner;
        @JdbcManager.Column(value = "product_name")
        public String productName;
        @JdbcManager.Column(value = "cost")
        public double cost;
        @JdbcManager.Column(value = "start_date")
        public String startDate;
    }

    public class User {
        private String userName;
        private HashMap<String, Resource> resources;

        private double totalCost;

        public User(String userName) {
            this.userName = userName;
            this.resources = new HashMap<>();
            this.totalCost = 0;
        }

        public void addResource(Resource resource) {
            this.resources.put(resource.getResourceName(), resource);
        }

        public String getUserName() {
            return userName;
        }

        public HashMap<String, Resource> getResources() {
            return resources;
        }

        public double calculateTotalCost() {
            for (Resource resource : resources.values()) {
                for (Day day : resource.getDays().values()) {
                    this.totalCost += day.getDailyCost();
                }
            }
            return this.totalCost;
        }
    }

    public class Resource {
        private String resourceName;
        private HashMap<String, Day> days;

        public Resource(String resourceName) {
            this.resourceName = resourceName;
            this.days = new HashMap<>();
        }

        public void addDay(String key, Day day) {
            this.days.put(key, day);
        }

        public String getResourceName() {
            return resourceName;
        }

        public HashMap<String, Day> getDays() {
            return days;
        }
    }

    public class Day {
        private Calendar date;
        private double dailyCost;

        public Day(Calendar date, double dailyCost) {
            this.date = date;
            this.dailyCost = dailyCost;
        }

        public Calendar getDate() {
            return date;
        }

        public double getDailyCost() {
            return dailyCost;
        }
    }
}