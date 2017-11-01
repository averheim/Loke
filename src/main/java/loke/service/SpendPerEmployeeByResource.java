package loke.service;

import com.googlecode.charts4j.*;
import loke.HtmlTableCreator;
import loke.db.athena.AthenaClient;
import loke.db.athena.JdbcManager;
import loke.model.Report;
import loke.utils.CalendarGenerator;
import loke.utils.DecimalFormatter;
import loke.utils.ResourceLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.googlecode.charts4j.Color.*;

public class SpendPerEmployeeByResource implements Service {
    private static final Logger log = LogManager.getLogger(SpendPerEmployeeByResource.class);
    private static final String SQL_QUERY = ResourceLoader.getResource("sql/SpendPerEmployeeByResource.sql");
    private static final List<Calendar> DAYS_BACK = CalendarGenerator.getDaysBack(60);
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private AthenaClient athenaClient;
    private String userOwnerRegExp;
    private HtmlTableCreator htmlTableCreator;
    private int colorCounter = 0;

    public SpendPerEmployeeByResource(AthenaClient athenaClient, String userOwnerRegExp, HtmlTableCreator htmlTableCreator) {
        this.athenaClient = athenaClient;
        this.userOwnerRegExp = userOwnerRegExp;
        this.htmlTableCreator = htmlTableCreator;
    }


    @Override
    public List<Report> getReports() {
        Map<String, User> users = sendRequest();
        return generateReports(users);
    }

    private List<Report> generateReports(Map<String, User> users) {
        List<Report> reports = new ArrayList<>();
        for (User user : users.values()) {
            resetColor();
            Scale scale = checkScale(user);
            List<String> xAxisLabels = getXAxisLabels();
            List<Line> lineChartPlots = createPlots(user, scale);
            LineChart chart = GCharts.newLineChart(lineChartPlots);
            configureChart(xAxisLabels, chart, user, scale);
            Report report = new Report(user.getUserName());
            report.addHtmlURL(chart.toURLString());
            report.addHtmlTable(generateHTMLTable(user));
            reports.add(report);
            log.info(report.getHtmlURLs() + "\n" + report.getHtmlTables());
        }
        return reports;
    }

    private String generateHTMLTable(User user) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM dd, YYYY");
        List<String> head = new ArrayList<>();
        head.add("Service");
        for (Calendar calendar : DAYS_BACK) {
            String date = simpleDateFormat.format(calendar.getTime());
            head.add(date);
        }
        head.add("Total");

        List<String> body = new ArrayList<>();
        double total = 0.0;
        for (Resource resource : user.getResources().values()) {
            body.add(resource.getResourceName() + " ($)");
            double resourceTotal = 0.0;
            for (Calendar calendar : DAYS_BACK) {
                Day day = resource.getDays().get(dateFormat.format(calendar.getTime()));
                String cost = day != null ? DecimalFormatter.format(day.getDailyCost(), 2) : "0.00";
                resourceTotal += day != null ? day.getDailyCost() : 0;
                body.add(cost);
            }
            total += resourceTotal;
            body.add(DecimalFormatter.format(resourceTotal, 2));
        }
        String foot = "Total: $" + DecimalFormatter.format(total, 2);
        return htmlTableCreator.createTable(head, body, foot, null, false);
    }

    private Scale checkScale(User user) {
        List<Double> dailyCosts = new ArrayList<>();

        for (Calendar calendar : DAYS_BACK) {
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

        // add labels
        for (Calendar day : DAYS_BACK) {
            String date = dateFormat.format(day.getTime());
            if (!labels.contains(date)) {
                labels.add(date.substring(8, 10));
            }
        }
        return labels;
    }

    private void configureChart(List<String> daysXAxisLabels, LineChart chart, User user, Scale scale) {
        int chartWidth = 1000;
        int chartHeight = 300;
        chart.addYAxisLabels(AxisLabelsFactory.newNumericAxisLabels(scale.getyAxisLabels()));
        chart.addXAxisLabels(AxisLabelsFactory.newAxisLabels(daysXAxisLabels));
        chart.addYAxisLabels(AxisLabelsFactory.newAxisLabels("Cost in " + scale.getSuffix(), 50));

        chart.addXAxisLabels(AxisLabelsFactory.newAxisLabels("Day", 50));
        chart.setSize(chartWidth, chartHeight);
        chart.setTitle("Total cost for " + user.getUserName() + " the past " + DAYS_BACK.size() + " days " + DecimalFormatter.format(user.calculateTotalCost(), 2) + " USD");
    }

    private List<Line> createPlots(User user, Scale scale) {
        List<Line> plots = new ArrayList<>();
        log.info(user.getUserName());
        for (Resource resource : user.getResources().values()) {
            List<Double> lineSizeValues = getLineSize(resource, scale);
            double total = getResourceTotal(resource);
            Line lineChartPlot = Plots.newLine(Data.newData(lineSizeValues), getNextColor(), resource.getResourceName() + " " + DecimalFormatter.format(total, 4));
            plots.add(0, lineChartPlot);
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

    private List<Double> getLineSize(Resource resource, Scale scale) {
        List<Double> lineSizeValues = new ArrayList<>();
        for (Double cost : getDailyCosts(resource)) {
            lineSizeValues.add(cost / scale.getDivideBy());
        }
        return lineSizeValues;
    }

    private List<Double> getDailyCosts(Resource resource) {
        List<Double> data = new ArrayList<>();
        for (Calendar calendar : DAYS_BACK) {
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
        if (colorCounter == colors.size()) {
            colorCounter = 0;
        }
        return color;
    }


    private Map<String, User> sendRequest() {
        Map<String, User> users = new HashMap<>();
        JdbcManager.QueryResult<SpendPerEmployeeByResourceDao> queryResult = athenaClient.executeQuery(SQL_QUERY, SpendPerEmployeeByResourceDao.class);
        for (SpendPerEmployeeByResourceDao dao : queryResult.getResultList()) {
            if (!dao.userOwner.matches(userOwnerRegExp)) {
                continue;
            }

            String userName = dao.userOwner;
            String startDate = dao.startDate;
            String productName = dao.productName;

            if (!users.containsKey(userName)) {
                users.put(userName, new User(userName));
            }
            if (!users.get(userName).getResources().containsKey(productName)) {
                users.get(userName).addResource(new Resource(productName));
            }

            Calendar date = Calendar.getInstance();
            try {
                date.setTime(dateFormat.parse(startDate));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Day day = new Day(date, dao.cost);
            users.get(userName).getResources().get(productName).addDay(dateFormat.format(day.getDate().getTime()), day);
        }
        return users;
    }

    public static class SpendPerEmployeeByResourceDao {
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

    private class Resource {
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

    private class Day {
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
