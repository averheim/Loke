package loke.services;

import com.googlecode.charts4j.*;
import com.googlecode.charts4j.Color;
import loke.utils.CalendarGenerator;
import loke.utils.DecimalFormatter;
import loke.utils.ResourceLoader;
import loke.db.athena.AthenaClient;
import loke.db.athena.JdbcManager;
import loke.model.Chart;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import loke.HtmlTableCreator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.googlecode.charts4j.BarChart.AUTO_RESIZE;
import static com.googlecode.charts4j.Color.*;

public class SpendPerUserDao implements Service {
    private static final Logger log = LogManager.getLogger(SpendPerUserDao.class);
    private AthenaClient athenaClient;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static final String SQL_QUERY = ResourceLoader.getResource("sql/CostPerUserAndProductLast30Days.sql");
    private HtmlTableCreator htmlTableCreator;
    private String userOwnerRegexp;


    private int colorCounter = 0;

    public SpendPerUserDao(AthenaClient athenaClient, HtmlTableCreator htmlTableCreator, String userOwnerRegexp) {
        this.athenaClient = athenaClient;
        this.htmlTableCreator = htmlTableCreator;
        this.userOwnerRegexp = userOwnerRegexp;
    }

    @Override
    public List<Chart> getCharts() {
        Map<String, User> users = sendRequest();
        return generateCharts(users);
    }

    private List<Chart> generateCharts(Map<String, User> users) {
        List<Chart> charts = new ArrayList<>();
        for (User user : users.values()) {
            resetColor();
            Scale scale = checkScale(user);
            List<String> xAxisLabels = getXAxisLabels();
            List<BarChartPlot> barChartPlots = createPlots(user, scale);
            BarChart chart = GCharts.newBarChart(barChartPlots);
            configureChart(xAxisLabels, chart, user, scale);
            Chart c = new Chart(user.getUserName());
            c.setHtmlURL(chart.toURLString());
            c.addHtmlTable(generateHTMLTable(user));
            charts.add(c);
            log.info(c.getHtmlURL() + "\n" + c.getHtmlTables());
        }
        return charts;
    }

    public String generateHTMLTable(User user) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM dd, YYYY");
        List<Calendar> calendars = CalendarGenerator.getDaysBack(30);

        List<String> head = new ArrayList<>();
        head.add("Service");
        for (Calendar calendar : calendars) {
            String date = simpleDateFormat.format(calendar.getTime());
            head.add(date);
        }
        head.add("Total");

        List<String> body = new ArrayList<>();
        double total = 0.0;
        for (Resource resource : user.getResources().values()) {
            body.add(resource.getResourceName());
            double resourceTotal = 0.0;
            for (Calendar calendar : calendars) {
                Day day = resource.getDays().get(dateFormat.format(calendar.getTime()));
                String cost = day != null ? DecimalFormatter.format(day.getDailyCost(), 2) : "00.00";
                resourceTotal += day != null ? day.getDailyCost() : 0;
                body.add(cost);
            }
            total += resourceTotal;
            body.add(DecimalFormatter.format(resourceTotal, 2));
        }
        String foot = "Total: $" + DecimalFormatter.format(total, 2);
        return htmlTableCreator.createTable(head, body, foot, null);

    }

    private Scale checkScale(User user) {
        List<Calendar> daysBack = CalendarGenerator.getDaysBack(30);
        List<Double> dailyCosts = new ArrayList<>();

        for (Calendar calendar : daysBack) {
            double dailyCost = 0.0;
            for (Resource resource : user.getResources().values()) {

                Day day = resource.getDays().get(dateFormat.format(calendar.getTime()));
                dailyCost += (day == null) ? 0.0 : day.getDailyCost();
            }
            dailyCosts.add(dailyCost);
        }

        dailyCosts.sort((o1, o2) -> (int) (o1 + o2));

        if (dailyCosts.get(0) > 100) return Scale.OVER_HUNDRED;
        if (dailyCosts.get(0) < 10) return Scale.UNDER_TEN;
        return Scale.UNDER_HUNDRED;
    }

    private List<String> getXAxisLabels() {
        List<String> labels = new ArrayList<>();
        List<Calendar> days = new ArrayList<>(CalendarGenerator.getDaysBack(30));

        // add labels
        for (Calendar day : days) {
            String date = dateFormat.format(day.getTime());
            if (!labels.contains(date)) {
                labels.add(date.substring(8, 10));
            }
        }
        return labels;
    }

    private void configureChart(List<String> daysXAxisLabels, BarChart chart, User user, Scale scale) {
        int chartWidth = 1000;
        int chartHeight = 300;
        chart.addYAxisLabels(AxisLabelsFactory.newNumericAxisLabels(scale.getyAxisLabels()));
        chart.addXAxisLabels(AxisLabelsFactory.newAxisLabels(daysXAxisLabels));
        chart.addYAxisLabels(AxisLabelsFactory.newAxisLabels("Cost in " + scale.getSuffix(), 50));

        chart.addXAxisLabels(AxisLabelsFactory.newAxisLabels("Day", 50));
        chart.setSize(chartWidth, chartHeight);
        chart.setBarWidth(AUTO_RESIZE);
        chart.setDataStacked(true);
        chart.setTitle("Total cost for " + user.getUserName() + " the past 30 days " + DecimalFormatter.format(user.calculateTotalCost(), 2) + " dollars");
    }

    private List<BarChartPlot> createPlots(User user, Scale scale) {
        List<BarChartPlot> plots = new ArrayList<>();
        log.info(user.getUserName());
        for (Resource resource : user.getResources().values()) {
            List<Double> barSizeValues = getBarSize(resource, scale);
            double total = getResourceTotal(resource);
            BarChartPlot barChartPlot = Plots.newBarChartPlot(Data.newData(barSizeValues), getNextColor(), resource.getResourceName() + " " + DecimalFormatter.format(total, 4));
            plots.add(0, barChartPlot);
        }
        return plots;
    }

    private double getResourceTotal(Resource resource) {
        double total = 0.0;
        for (Double cost : getDailyCosts(resource)) {
            total += cost;
        }
        return total;
    }

    private List<Double> getBarSize(Resource resource, Scale scale) {
        List<Double> barSizeValues = new ArrayList<>();
        for (Double cost : getDailyCosts(resource)) {
            barSizeValues.add(cost / scale.getDivideBy());
        }
        return barSizeValues;
    }

    private List<Double> getDailyCosts(Resource resource) {
        List<Double> data = new ArrayList<>();
        List<Calendar> calendars = CalendarGenerator.getDaysBack(30);
        for (Calendar calendar : calendars) {
            Day day = resource.getDays().get(dateFormat.format(calendar.getTime()));
            if (day == null) {
                data.add(0.0);
            } else {
                data.add(resource.getDays().get(dateFormat.format(calendar.getTime())).getDailyCost());
            }
        }
        return data;
    }

    private void resetColor() {
        this.colorCounter = 0;
    }

    private Color getNextColor() {
        List<Color> colors = new ArrayList<>();
        colors.add(BLUE);
        colors.add(RED);
        colors.add(YELLOW);
        colors.add(GREEN);
        colors.add(GRAY);
        colors.add(AQUAMARINE);
        colors.add(ORANGE);
        Color color = colors.get(colorCounter);
        colorCounter++;
        return color;
    }

    private Map<String, User> sendRequest() {
        Map<String, User> users = new HashMap<>();
        JdbcManager.QueryResult<SpendPerUser> queryResult = athenaClient.executeQuery(SQL_QUERY, SpendPerUser.class);
        for (SpendPerUser spendPerUser : queryResult.getResultList()) {
            if(!spendPerUser.userOwner.matches(userOwnerRegexp)) {
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

        public Resource getResource(String resourceName) {
            return this.resources.get(resourceName);
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