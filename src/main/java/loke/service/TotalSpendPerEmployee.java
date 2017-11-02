package loke.service;

import com.googlecode.charts4j.*;
import loke.db.athena.AthenaClient;
import loke.db.athena.JdbcManager;
import loke.model.Report;
import loke.model.TotalReport;
import loke.utils.CalendarGenerator;
import loke.utils.ColorPicker;
import loke.utils.DecimalFormatter;
import loke.utils.ResourceLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class TotalSpendPerEmployee implements Service {
    private static final Logger log = LogManager.getLogger(TotalSpendPerEmployee.class);
    private static final String SQL_QUERY = ResourceLoader.getResource("sql/TotalSpendPerEmployee.sql");
    private static final List<Calendar> DAYS_BACK = CalendarGenerator.getDaysBack(60);
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final AthenaClient athenaClient;
    private final String userOwnerRegExp;

    public TotalSpendPerEmployee(AthenaClient athena, String userOwnerRegExp) {
        this.athenaClient = athena;
        this.userOwnerRegExp = userOwnerRegExp;
    }


    @Override
    public List<Report> getReports() {
        Map<String, User> users = sendRequest();
        return generateReports(users);
    }

    private List<Report> generateReports(Map<String, User> users) {
        List<Report> reports = new ArrayList<>();
        for (User user : users.values()) {
            ColorPicker.resetColor();
            Scale scale = checkScale(user);
            List<String> xAxisLabels = getXAxisLabels();
            List<Line> lineChartPlots = createPlots(user, scale);
            LineChart chart = GCharts.newLineChart(lineChartPlots);
            configureChart(xAxisLabels, chart, user, scale);
            Report report = new TotalReport(user.getUserName());
            report.addHtmlURL(chart.toURLString());
            reports.add(report);
            log.info(report.getHtmlURLs() + "\n" + report.getHtmlTables());
        }
        return reports;
    }

    private Scale checkScale(User user) {
        List<Double> dailyCosts = new ArrayList<>();

        for (Calendar calendar : DAYS_BACK) {
            Day day = user.getDays().get(dateFormat.format(calendar.getTime()));
            dailyCosts.add((day == null) ? 0.0 : day.getDailyCost());
        }

        dailyCosts.sort((o1, o2) -> Double.compare(o2, o1));
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
        List<Double> lineSizeValues = getLineSize(user, scale);
        Line lineChartPlot = Plots.newLine(Data.newData(lineSizeValues), ColorPicker.getNextColor());
        plots.add(0, lineChartPlot);
        return plots;
    }

    private List<Double> getLineSize(User user, Scale scale) {
        List<Double> lineSizeValues = new ArrayList<>();
        for (Calendar calendar : DAYS_BACK) {
            Day day = user.getDays().get(dateFormat.format(calendar.getTime()));
            lineSizeValues.add((day != null) ? day.getDailyCost() / scale.getDivideBy() : 0);
        }
        return lineSizeValues;
    }

    private Map<String, User> sendRequest() {
        Map<String, User> users = new HashMap<>();
        JdbcManager.QueryResult<TotalSpendPerEmployeeDao> queryResult = athenaClient.executeQuery(SQL_QUERY, TotalSpendPerEmployeeDao.class);
        for (TotalSpendPerEmployeeDao dao : queryResult.getResultList()) {
            if (!dao.userOwner.matches(userOwnerRegExp)) {
                continue;
            }

            String userName = dao.userOwner;
            String startDate = dao.startDate;

            if (!users.containsKey(userName)) {
                users.put(userName, new User(userName));
            }

            Calendar date = Calendar.getInstance();
            try {
                date.setTime(dateFormat.parse(startDate));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Day day = new Day(date, dao.cost);
            users.get(userName).addDay(dateFormat.format(day.getDate().getTime()), day);
        }
        return users;
    }

    public static class TotalSpendPerEmployeeDao {
        @JdbcManager.Column(value = "user_owner")
        public String userOwner;
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

        public String getUserName() {
            return userName;
        }

        public Map<String, Day> getDays() {
            return days;
        }

        public double calculateTotalCost() {
            double totalCost = 0;
            for (Day day : days.values()) {
                totalCost += day.getDailyCost();
            }
            return totalCost;
        }

        public void addDay(String key, Day day) {
            days.put(key, day);
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
