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

public class SpendPerUserByAccountDao implements Service {
    private static final Logger log = LogManager.getLogger(SpendPerUserByAccountDao.class);
    private AthenaClient athenaClient;
    private HtmlTableCreator htmlTableCreator;
    private String userOwnerRegExp;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static final String SQL_QUERY = ResourceLoader.getResource("sql/SpendPerUserByAccount.sql");
    private double showAccountThreshold;
    private static final List<Calendar> THIRTY_DAYS_BACK = CalendarGenerator.getDaysBack(60);
    private int colorCounter = 0;
    private double accountTotal = 0;
    private double total = 0;

    public SpendPerUserByAccountDao(AthenaClient athenaClient, HtmlTableCreator htmlTableCreator, String userOwnerRegExp, double showAccountThreshold) {
        this.athenaClient = athenaClient;
        this.htmlTableCreator = htmlTableCreator;
        this.userOwnerRegExp = userOwnerRegExp;
        this.showAccountThreshold = showAccountThreshold;
    }

    @Override
    public List<Report> getReports() {
        Map<String, User> users = sendRequest();
        return generateReports(users);
    }

    private List<Report> generateReports(Map<String, User> users) {
        List<Report> charts = new ArrayList<>();
        for (User user : users.values()) {
            Report report = new Report(user.getUserOwner());
            report.addHtmlURLs(generateHtmlURLs(user));
            report.addHtmlTables(generateHTMLTables(user));
            for (String s : generateHtmlURLs(user)) {
                log.info("URL FOR " + user.getUserOwner() + ": " + s);
            }
            charts.add(report);
            log.info(report.getOwner() + "\n" + report.getHtmlTables());
        }
        return charts;
    }

    private List<String> generateHtmlURLs(User user) {
        List<String> htmlURLs = new ArrayList<>();
        log.info("USER: " + user.getUserOwner());
        for (Account account : user.getAccounts().values()) {
            Scale scale = checkScale(account);
            log.info("SCALE: " + scale);
            List<String> xAxisLabels = getXAxisLabels();
            List<Line> lineChartPlots = createPlots(account, scale);
            LineChart chart = GCharts.newLineChart(lineChartPlots);
            configureChart(xAxisLabels, chart, account, scale, user.getUserOwner());
            htmlURLs.add(chart.toURLString());
        }
        return htmlURLs;
    }

    private void configureChart(List<String> daysXAxisLabels, LineChart chart, Account account, Scale scale, String userName) {
        int chartWidth = 1000;
        int chartHeight = 300;
        chart.addYAxisLabels(AxisLabelsFactory.newNumericAxisLabels(scale.getyAxisLabels()));
        chart.addXAxisLabels(AxisLabelsFactory.newAxisLabels(daysXAxisLabels));
        chart.addYAxisLabels(AxisLabelsFactory.newAxisLabels("Cost in " + scale.getSuffix(), 50));

        chart.addXAxisLabels(AxisLabelsFactory.newAxisLabels("Day", 50));
        chart.setSize(chartWidth, chartHeight);
        String total = DecimalFormatter.format(calculateAccountTotal(account), 2);
        chart.setTitle("Total cost for " + userName + " in " + account.getAccountId() + " the past 30 days. " + total + " UDS total.");
    }

    private double calculateAccountTotal(Account account) {
        double total = 0;
        for (Resource resource : account.getResources().values()) {
            total += getResourceTotal(resource);
        }
        return total;
    }

    private List<Line> createPlots(Account account, Scale scale) {
        List<Line> plots = new ArrayList<>();
        for (Resource resource : account.getResources().values()) {
            List<Double> lineSizeValues = getLineSize(resource, scale);
            double total = getResourceTotal(resource);
            Line lineChartPlot = Plots.newLine(
                    Data.newData(lineSizeValues), getNextColor(),
                    resource.getProductName() + " " + DecimalFormatter.format(total, 2));
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
        for (Calendar calendar : THIRTY_DAYS_BACK) {
            Day day = resource.getDays().get(dateFormat.format(calendar.getTime()));
            if (day == null) {
                data.add(0.0);
            } else {
                data.add(resource.getDays().get(dateFormat.format(calendar.getTime())).getDailyCost());
            }
        }
        return data;
    }

    private Scale checkScale(Account account) {
        List<Double> dailyCosts = new ArrayList<>();

        for (Calendar calendar : THIRTY_DAYS_BACK) {
            double dailyCost = 0.0;
            for (Resource resource : account.getResources().values()) {
                Day day = resource.getDays().get(dateFormat.format(calendar.getTime()));
                dailyCost += (day == null) ? 0 : day.getDailyCost();
            }
            dailyCosts.add(dailyCost);
        }

        dailyCosts.sort((o1, o2) -> Double.compare(o2, o1));
        for (Double dailyCost : dailyCosts) {
            log.info("DAILY COST: " + dailyCost);
        }

        log.info("THE FIRST ONE: " + dailyCosts.get(0));
        if (dailyCosts.get(0) > 100) return Scale.OVER_HUNDRED;
        if (dailyCosts.get(0) < 10) return Scale.UNDER_TEN;
        return Scale.UNDER_HUNDRED;
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

    private List<String> getXAxisLabels() {
        List<String> labels = new ArrayList<>();

        // add labels
        for (Calendar day : THIRTY_DAYS_BACK) {
            String date = dateFormat.format(day.getTime());
            if (!labels.contains(date)) {
                labels.add(date.substring(8, 10));
            }
        }
        return labels;
    }

    private List<String> generateHTMLTables(User user) {
        List<String> htmlTables = new ArrayList<>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM dd, YYYY", Locale.US);
        List<String> head = new ArrayList<>();

        head.add("Products by Account");
        for (Calendar calendar : THIRTY_DAYS_BACK) {
            head.add(simpleDateFormat.format(calendar.getTime()));
        }
        head.add("Total ($)");

        List<List<String>> bodies = new ArrayList<>();
        for (Account account : user.getAccounts().values()) {
            List<Resource> resources = new ArrayList<>(account.getResources().values());
            Collections.reverse(resources);

            if (accountTotal >= showAccountThreshold) {
                bodies.add(getAccountTotalRows(THIRTY_DAYS_BACK, account, resources));
                bodies.add(getResourceRows(THIRTY_DAYS_BACK, resources));
            }
            resources.clear();
        }
        bodies.add(getTotalCostRow(user, THIRTY_DAYS_BACK));


        String heading = "Monthly spend for " + user.getUserOwner() + " (Accounts with total cost below $"
                + DecimalFormatter.format(showAccountThreshold, 2) + " will not be shown)";
        String footer = "Monthly total: $" + DecimalFormatter.format(total, 2);

        if (bodies.size() > 1) {
            htmlTables.add(htmlTableCreator.createMarkedRowTable(head, bodies, footer, heading, "Total"));
        }

        return htmlTables;
    }

    private List<String> getAccountTotalRows(List<Calendar> calendarDaysBack, Account account, List<Resource> resources) {
        List<String> accountRows = new ArrayList<>();
        accountRows.add(account.getAccountId() + " Total ($)");
        accountTotal = 0;
        for (Calendar calendar : calendarDaysBack) {
            double dailyTotal = 0.00;
            for (Resource resource : resources) {
                Day day = resource.days.get(dateFormat.format(calendar.getTime()));
                if (day != null) {
                    dailyTotal += day.getDailyCost();
                }
            }
            if (dailyTotal > 0) {
                accountRows.add(DecimalFormatter.format(dailyTotal, 2));
            } else {
                accountRows.add("0.00");
            }
            accountTotal += dailyTotal;
        }
        accountRows.add(DecimalFormatter.format(accountTotal, 2));
        return accountRows;
    }

    private List<String> getResourceRows(List<Calendar> calendarDaysBack, List<Resource> resources) {
        List<String> resourceRows = new ArrayList<>();
        for (Resource resource : resources) {
            resourceRows.add(resource.productName + " ($)");
            double resourceTotal = 0.00;

            for (Calendar calendar : calendarDaysBack) {
                Day day = resource.getDays().get(dateFormat.format(calendar.getTime()));
                if (day != null) {
                    resourceRows.add(DecimalFormatter.format(day.getDailyCost(), 2));
                    resourceTotal += day.getDailyCost();
                } else {
                    resourceRows.add("0.00");
                }
            }
            resourceRows.add(DecimalFormatter.format(resourceTotal, 2));
        }
        return resourceRows;
    }

    private List<String> getTotalCostRow(User user, List<Calendar> calendarDaysBack) {
        List<String> totalCostRows = new ArrayList<>();
        totalCostRows.add("Total for all accounts ($)");
        total = 0;
        for (Calendar calendar : calendarDaysBack) {
            double dailyTotal = 0;
            for (Account account : user.getAccounts().values()) {
                for (Resource resource : account.getResources().values()) {
                    Day day = resource.getDays().get(dateFormat.format(calendar.getTime()));
                    if (day != null) {
                        dailyTotal += day.getDailyCost();
                    }
                }
            }
            total += dailyTotal;
            totalCostRows.add((dailyTotal != 0) ? DecimalFormatter.format(dailyTotal, 2) : "0.00");
        }
        totalCostRows.add((total != 0) ? DecimalFormatter.format(total, 2) : "0.00");
        return totalCostRows;
    }

    private Map<String, User> sendRequest() {
        Map<String, User> users = new HashMap<>();
        JdbcManager.QueryResult<SpendPerUserAndAccount> queryResult = athenaClient.executeQuery(SQL_QUERY, SpendPerUserAndAccount.class);
        for (SpendPerUserAndAccount spendPerUserAndAccount : queryResult.getResultList()) {
            if (!spendPerUserAndAccount.userOwner.matches(userOwnerRegExp)) {
                continue;
            }

            if (!users.containsKey(spendPerUserAndAccount.userOwner)) {
                users.put(spendPerUserAndAccount.userOwner, new User(spendPerUserAndAccount.userOwner));
            }

            User user = users.get(spendPerUserAndAccount.userOwner);
            if (!user.getAccounts().containsKey(spendPerUserAndAccount.accountId)) {
                user.addAccount(spendPerUserAndAccount.accountId, new Account(spendPerUserAndAccount.accountId));
            }

            Account account = user.getAccounts().get(spendPerUserAndAccount.accountId);
            if (!account.getResources().containsKey(spendPerUserAndAccount.productName)) {
                account.addResource(spendPerUserAndAccount.productName, new Resource(spendPerUserAndAccount.productName));
            }

            Resource resource = account.getResources().get(spendPerUserAndAccount.productName);
            Calendar date = Calendar.getInstance();
            try {
                date.setTime(dateFormat.parse(spendPerUserAndAccount.startDate));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Day day = new Day(date, spendPerUserAndAccount.cost);
            resource.getDays().put(dateFormat.format(day.getDate().getTime()), day);
        }
        return users;
    }

    public static class SpendPerUserAndAccount {
        @JdbcManager.Column(value = "user_owner")
        public String userOwner;
        @JdbcManager.Column(value = "account_name")
        public String accountId;
        @JdbcManager.Column(value = "product_name")
        public String productName;
        @JdbcManager.Column(value = "start_date")
        public String startDate;
        @JdbcManager.Column(value = "cost")
        public double cost;
    }

    private class User {
        private String userOwner;
        private Map<String, Account> accounts;

        public User(String userOwner) {
            this.userOwner = userOwner;
            this.accounts = new HashMap<>();
        }

        public String getUserOwner() {
            return userOwner;
        }

        public Map<String, Account> getAccounts() {
            return accounts;
        }

        public void addAccount(String key, Account account) {
            this.accounts.put(key, account);
        }
    }

    private class Account {
        private String accountId;
        private Map<String, Resource> resources;

        public Account(String accountId) {
            this.accountId = accountId;
            this.resources = new HashMap<>();
        }

        public String getAccountId() {
            return accountId;
        }

        public Map<String, Resource> getResources() {
            return resources;
        }

        public void addResource(String key, Resource resource) {
            this.resources.put(key, resource);
        }
    }

    private class Resource {
        private String productName;
        private Map<String, Day> days;

        public Resource(String productName) {
            this.productName = productName;
            this.days = new HashMap<>();
        }

        public String getProductName() {
            return productName;
        }

        public Map<String, Day> getDays() {
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
