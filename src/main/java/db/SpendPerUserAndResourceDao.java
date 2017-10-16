package db;

import com.googlecode.charts4j.*;
import db.athena.AthenaClient;
import db.athena.JdbcManager;
import model.Chart;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.googlecode.charts4j.Color.RED;
import static com.googlecode.charts4j.Color.WHITE;

public class SpendPerUserAndResourceDao implements Service {
    private static final Logger log = LogManager.getLogger(SpendPerUserAndResourceDao.class);
    private AthenaClient athenaClient;
    private static final String SQL_QUERY = ResourceLoader.getResource("sql/CostPerUserAndProductLast30Days.sql");

    public SpendPerUserAndResourceDao(AthenaClient athenaClient) {
        this.athenaClient = athenaClient;
    }

    @Override
    public List<Chart> getCharts() {
        Map<String, User> users = sendRequest();
        return generateCharts(users);
    }

    private List<Chart> generateCharts(Map<String, User> users) {
        log.trace("Generating SpendPerUserAndResrouce charts");
        List<Chart> charts = new ArrayList<>();
        for (User user : users.values()) {
            Map<String, Day> days = user.getDays();
            List<Double> dailyTotals = new ArrayList<>();
            List<String> dates = new ArrayList<>();
            List<Line> lines = new ArrayList<>();

            for (Day day : days.values()) {
                double dailyTotal = 0;
                List<Resource> resources = day.getResources();
                dates.add(day.getDate().substring(5));
                for (Resource resource : resources) {
                    dailyTotal += resource.getCost();


                }
                dailyTotals.add(dailyTotal);
            }
            lines.add(Plots.newLine(Data.newData(dailyTotals)));

            LineChart chart = GCharts.newLineChart(lines);
            chart.setTitle("Daily total for " + user.getUserName(), RED, 14);
            chart.setSize(1000, 300);
            chart.addYAxisLabels(AxisLabelsFactory.newAxisLabels("0", "1"));
            chart.addXAxisLabels(AxisLabelsFactory.newAxisLabels(dates));

            // Defining background and chart fills.
            chart.setBackgroundFill(Fills.newSolidFill(WHITE));
            final LinearGradientFill fill = Fills.newLinearGradientFill(0, WHITE, 100);
            fill.addColorAndOffset(WHITE, 0);
            chart.setAreaFill(fill);
            System.out.println(chart.toURLString());


            charts.add(new Chart(user.getUserName(), "url"));
        }
        log.trace("Done generating charts");
        return charts;
    }

    private Map<String, User> sendRequest() {
        Map<String, User> users = new HashMap<>();
        JdbcManager.QueryResult<SpendPerUserAndResource> spendPerUserAndResourceQueryResult = athenaClient.executeQuery(SQL_QUERY, SpendPerUserAndResource.class);
        for (SpendPerUserAndResource spendPerUserAndResource : spendPerUserAndResourceQueryResult.getResultList()) {
            String userOwner = spendPerUserAndResource.userOwner;
            String startDate = spendPerUserAndResource.startDate;
            String productName = spendPerUserAndResource.productName;
            double cost = spendPerUserAndResource.cost;

            if (!users.containsKey(userOwner)) {
                users.put(userOwner, new User(userOwner));
            }

            if (!users.get(userOwner).getDays().containsKey(startDate)) {
                users.get(userOwner).getDays().put(startDate, new Day(startDate));
            }

            users.get(userOwner).getDays().get(startDate).addResource(new Resource(productName, cost));
        }
        return users;
    }

    public static class SpendPerUserAndResource {
        @JdbcManager.Column(value = "user_owner")
        public String userOwner;
        @JdbcManager.Column(value = "product_name")
        public String productName;
        @JdbcManager.Column(value = "cost")
        public double cost;
        @JdbcManager.Column(value = "start_date")
        public String startDate;
    }

    private class User {
        private String userName;
        private Map<String, Day> days;

        public User(String userName) {
            this.userName = userName;
            this.days = new HashMap<>();
        }

        public void addDay(String date, Day day) {
            this.days.put(date, day);
        }

        public String getUserName() {
            return userName;
        }

        public Map<String, Day> getDays() {
            return days;
        }
    }

    private class Day {
        private String date;
        private List<Resource> resources;

        public Day(String date) {
            this.date = date;
            this.resources = new ArrayList<>();
        }

        public void addResource(Resource resource) {
            resources.add(resource);
        }

        public String getDate() {
            return date;
        }

        public List<Resource> getResources() {
            return resources;
        }
    }

    private class Resource {
        private String productName;
        private double cost;

        public Resource(String productName, double cost) {
            this.productName = productName;
            this.cost = cost;
        }

        public String getProductName() {
            return productName;
        }

        public double getCost() {
            return cost;
        }
    }
}
