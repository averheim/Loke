package services.spendperuserlast30days;

import com.googlecode.charts4j.*;
import com.googlecode.charts4j.Color;
import utils.ResourceLoader;
import db.athena.AthenaClient;
import db.athena.JdbcManager;
import model.Chart;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import services.Service;
import utils.HtmlTableCreator;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.googlecode.charts4j.BarChart.AUTO_RESIZE;
import static com.googlecode.charts4j.Color.*;

public class SpendPerUserAndResourceDao implements Service {
    private static final Logger log = LogManager.getLogger(SpendPerUserAndResourceDao.class);
    private AthenaClient athenaClient;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static final String SQL_QUERY = ResourceLoader.getResource("sql/CostPerUserAndProductLast30Days.sql");

    private int colorCounter = 0;

    public SpendPerUserAndResourceDao(AthenaClient athenaClient) {
        this.athenaClient = athenaClient;
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
            Chart c = new Chart(user.getUserName(), chart.toURLString());
            c.setHtmlTable(generateHTMLTable_1(user));
            charts.add(c);
            log.info(c.getHtmlURL() + "\n" + c.getHtmlTable());
        }
        return charts;
    }

    public String generateHTMLTable_1(User user) {
        HtmlTableCreator htmlTableCreator = new HtmlTableCreator();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM dd, YYYY");
        List<Calendar> calendars = getDaysBack(30);

        List<String> head = new ArrayList<>();
        head.add("Service");
        for (Calendar calendar : calendars) {
            String date = simpleDateFormat.format(calendar.getTime());
            head.add(date);
        }
        head.add("Total");

        List<String> body = new ArrayList<>();
        for (Resource resource : user.getResources().values()) {
            body.add(resource.getResourceName());
            double total = 0.0;
            for (Calendar calendar : calendars) {
                Day day = resource.getDays().get(dateFormat.format(calendar.getTime()));
                String cost = day != null ? decimalFormat(day.getDailyCost(), 2) : "00.00";
                total += day != null ? day.getDailyCost() : 0;
                body.add(cost);
            }
            body.add(decimalFormat(total, 2));
        }




        return htmlTableCreator.createTable(head, body, null);

    }

    private Scale checkScale(User user) {
        List<Calendar> daysBack = getDaysBack(30);
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

    private List<Calendar> getDaysBack(int amount) {
        List<Calendar> days = new ArrayList<>();

        for (int i = 0; i < amount; i++) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, -i);
            days.add(0, calendar);
        }
        return days;
    }

    private List<String> getXAxisLabels() {
        List<String> labels = new ArrayList<>();
        List<Calendar> days = new ArrayList<>(getDaysBack(30));

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
        chart.setTitle("Total cost for " + user.getUserName() + " the past 30 days " + decimalFormat(user.calculateTotalCost(), 2) + " dollars");
    }

    private List<BarChartPlot> createPlots(User user, Scale scale) {
        List<BarChartPlot> plots = new ArrayList<>();
        log.info(user.getUserName());
        for (Resource resource : user.getResources().values()) {
            List<Double> barSizeValues = getBarSize(resource, scale);
            double total = getResourceTotal(resource);
            BarChartPlot barChartPlot = Plots.newBarChartPlot(Data.newData(barSizeValues), getNextColor(), resource.getResourceName() + " " + decimalFormat(total, 4));
            plots.add(0, barChartPlot);
        }
        return plots;
    }

    private String decimalFormat(double aDouble, int decimals) {
        StringBuilder pattern = new StringBuilder("#.");
        for (int i = 0; i < decimals; i++) {
            pattern.append("#");
        }
        DecimalFormat decimalFormat = new DecimalFormat(pattern.toString());
        decimalFormat.setRoundingMode(RoundingMode.CEILING);
        return decimalFormat.format(aDouble).replace(',', '.');
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
        List<Calendar> calendars = getDaysBack(30);
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
        JdbcManager.QueryResult<SpendPerUserAndResource> spendPerUserAndResourceQueryResult = athenaClient.executeQuery(SQL_QUERY, SpendPerUserAndResource.class);
        for (SpendPerUserAndResource spendPerUserAndResource : spendPerUserAndResourceQueryResult.getResultList()) {
            String userOwner = spendPerUserAndResource.userOwner;
            String startDate = spendPerUserAndResource.startDate;
            String productName = spendPerUserAndResource.productName;
            double cost = spendPerUserAndResource.cost;

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
            Day day = new Day(date, cost);
            users.get(userOwner).getResources().get(productName).addDay(dateFormat.format(day.getDate().getTime()), day);
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
/*
    private String createHtmlTable(User user) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM dd, YYYY");
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<div style=\"overflow-x: scroll;\">");
        stringBuilder.append("<table class=\"table table-hover table-bordered table-responsive\">");
        stringBuilder.append("<thead><th>Service</th>");

        for (Calendar day : getDaysBack(30)) {
            String date = simpleDateFormat.format(day.getTime());
            stringBuilder.append("<th>").append(date).append("</th>");
        }

        stringBuilder.append("<th>Total</th>");
        stringBuilder.append("</thead><tbody>");

        double total = 0;
        for (Resource resource : user.getResources().values()) {
            stringBuilder.append("<tr>");
            stringBuilder.append("<td>").append(resource.getResourceName()).append("</td>");
            for (Calendar calendar : getDaysBack(30)) {
                Day day = resource.getDays().get(dateFormat.format(calendar.getTime()));
                String cost = day != null ? decimalFormat(day.getDailyCost(), 2) : "00.00";
                stringBuilder.append("<td>").append("$").append(cost).append("</td>");
            }
            double resourceTotal = 0;
            for (Day day : resource.getDays().values()) {
                resourceTotal += day.getDailyCost();
                total += day.getDailyCost();
            }
            stringBuilder.append("<td>").append("$").append(decimalFormat(resourceTotal, 2)).append("</td>");
        }
        stringBuilder.append("</tbody><tfoot><tr><td colspan=\"32\">Total: $").append(decimalFormat(total, 2)).append("</td></tr></tfoot>");
        stringBuilder.append("</table></div>");
        return stringBuilder.toString();
    }
    */
}