package loke.service;

import com.googlecode.charts4j.*;
import loke.db.athena.AthenaClient;
import loke.db.athena.JdbcManager;
import loke.model.Report;
import loke.utils.CalendarGenerator;
import loke.utils.ColorPicker;
import loke.utils.DecimalFormatter;
import loke.utils.ResourceLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class SpendPerEmployeeByAccount implements Service {
    private static final Logger log = LogManager.getLogger(SpendPerEmployeeByAccount.class);
    private static final String SQL_QUERY = ResourceLoader.getResource("sql/SpendPerEmployeeByAccount.sql");
    private static final List<Calendar> DAYS_BACK = CalendarGenerator.getDaysBack(60);
    private AthenaClient athenaClient;
    private String userOwnerRegExp;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private double showAccountThreshold;
    private Map<String, String> csvAccounts;
    private VelocityEngine velocityEngine;

    public SpendPerEmployeeByAccount(AthenaClient athenaClient, String userOwnerRegExp, double showAccountThreshold, Map<String, String> csvAccounts, VelocityEngine velocityEngine) {
        this.athenaClient = athenaClient;
        this.userOwnerRegExp = userOwnerRegExp;
        this.showAccountThreshold = showAccountThreshold;
        this.csvAccounts = csvAccounts;
        this.velocityEngine = velocityEngine;
    }

    @Override
    public List<Report> getReports() {
        Map<String, User> users = sendRequest();
        return generateReports(users);
    }

    private List<Report> generateReports(Map<String, User> users) {
        List<Report> reports = new ArrayList<>();
        for (User user : users.values()) {
            Report report = new Report(user.getUserName());
            report.setChartUrl(generateChartUrl(user));
            report.setHtmlTable(generateHTMLTable(user));
            reports.add(report);
            log.info("Report generated for: {}", user.getUserName());
        }
        return reports;
    }

    private String generateChartUrl(User user) {
        ColorPicker.resetColor();
        Scale scale = checkScale(user.getAccounts().values());
        List<String> xAxisLabels = getXAxisLabels();
        List<Line> lineChartPlots = createPlots(user, scale);
        LineChart chart = GCharts.newLineChart(lineChartPlots);
        configureChart(xAxisLabels, chart, user, scale, user.getUserName());
        return chart.toURLForHTML();
    }

    private void configureChart(List<String> daysXAxisLabels, LineChart chart, User user, Scale scale, String userName) {
        int chartWidth = 1000;
        int chartHeight = 300;
        chart.addYAxisLabels(AxisLabelsFactory.newNumericAxisLabels(scale.getyAxisLabels()));
        chart.addXAxisLabels(AxisLabelsFactory.newAxisLabels(daysXAxisLabels));
        chart.addYAxisLabels(AxisLabelsFactory.newAxisLabels("Cost in " + scale.getSuffix(), 50));
        chart.addXAxisLabels(AxisLabelsFactory.newAxisLabels("Day", 50));
        chart.setSize(chartWidth, chartHeight);
        chart.setTitle("Total spend for "
                + userName
                + " in by account the past "
                + DAYS_BACK.size()
                + " days. "
                + DecimalFormatter.format(user.calculateTotalCost(), 2) + " UDS total.");
    }

    private List<Line> createPlots(User user, Scale scale) {
        List<Line> plots = new ArrayList<>();

        for (Account account : user.getAccounts().values()) {
            List<Double> lineSizeValues = new ArrayList<>();
            for (Calendar calendar : DAYS_BACK) {
                lineSizeValues.add(
                        account.getAccountDailyTotal(dateFormat.format(calendar.getTime())) / scale.getDivideBy()
                );

            }
            Line lineChartPlot = Plots.newLine(
                    Data.newData(lineSizeValues),
                    ColorPicker.getNextColor(),
                    account.getAccountId() + " " + DecimalFormatter.format(account.getAccountTotal(), 2));
            plots.add(0, lineChartPlot);
        }
        return plots;
    }

    private Scale checkScale(Collection<Account> accounts) {
        List<Double> dailyCosts = new ArrayList<>();

        for (Account account : accounts) {
            for (Calendar calendar : DAYS_BACK) {
                dailyCosts.add(account.getAccountDailyTotal(dateFormat.format(calendar.getTime())));
            }
        }

        dailyCosts.sort((o1, o2) -> Double.compare(o2, o1));

        if (dailyCosts.get(0) > 100) return Scale.OVER_HUNDRED;
        if (dailyCosts.get(0) < 10) return Scale.UNDER_TEN;
        return Scale.UNDER_HUNDRED;
    }

    private List<String> getXAxisLabels() {
        List<String> labels = new ArrayList<>();

        for (Calendar day : DAYS_BACK) {
            String date = dateFormat.format(day.getTime());
            if (!labels.contains(date)) {
                labels.add(date.substring(8, 10));
            }
        }
        return labels;
    }

    private String generateHTMLTable(User user) {
        if (user.calculateTotalCost() < showAccountThreshold) {
            return null;
        }
            VelocityContext context = new VelocityContext();
            context.put("userName", user.getUserName());
            context.put("showAccountThreshold", this.showAccountThreshold);
            context.put("dates", DAYS_BACK);
            context.put("accounts", user.getAccounts().values());
            context.put("total", user.calculateTotalCost());
            context.put("colspan", DAYS_BACK.size() + 2);
            context.put("simpleDateForamt", new SimpleDateFormat("MMM dd, YYYY", Locale.US));
            context.put("dateFormat", this.dateFormat);
            context.put("decimalFormatter", DecimalFormatter.class);

            Template template = velocityEngine.getTemplate("src/templates/spendperemployeebyaccount.vm");

            StringWriter stringWriter = new StringWriter();
            template.merge(context, stringWriter);

            return stringWriter.toString().trim();
    }

    private Map<String, User> sendRequest() {
        log.info("Fetching data and mapping objects");
        Map<String, User> users = new HashMap<>();
        JdbcManager.QueryResult<SpendPerEmployeeAndAccountDao> queryResult = athenaClient.executeQuery(SQL_QUERY, SpendPerEmployeeAndAccountDao.class);
        for (SpendPerEmployeeAndAccountDao dao : queryResult.getResultList()) {
            if (!dao.userOwner.matches(userOwnerRegExp)) {
                continue;
            }

            if (!users.containsKey(dao.userOwner)) {
                users.put(dao.userOwner, new User(dao.userOwner));
            }

            User user = users.get(dao.userOwner);
            if (!user.getAccounts().containsKey(dao.accountId)) {
                user.addAccount(dao.accountId, new Account(dao.accountId));
            }

            Account account = user.getAccounts().get(dao.accountId);
            if (!account.getResources().containsKey(dao.productName)) {
                account.addResource(dao.productName, new Resource(dao.productName));
            }

            Resource resource = account.getResources().get(dao.productName);
            Calendar date = Calendar.getInstance();
            try {
                date.setTime(dateFormat.parse(dao.startDate));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            String accountId = dao.accountId;
            String accountName = csvAccounts.get(accountId);
            dao.accountId = (accountName != null) ? accountName : accountId;

            Day day = new Day(date, dao.cost);
            resource.getDays().put(dateFormat.format(day.getDate().getTime()), day);
        }
        log.info("Done mapping objects");
        return users;
    }

    public static class SpendPerEmployeeAndAccountDao {
        @JdbcManager.Column(value = "user_owner")
        public String userOwner;
        @JdbcManager.Column(value = "account_id")
        public String accountId;
        @JdbcManager.Column(value = "product_name")
        public String productName;
        @JdbcManager.Column(value = "start_date")
        public String startDate;
        @JdbcManager.Column(value = "cost")
        public double cost;
    }

    public class User {
        private String userName;
        private Map<String, Account> accounts;

        public User(String userName) {
            this.userName = userName;
            this.accounts = new HashMap<>();
        }

        public double calculateTotalCost() {
            double total = 0;
            for (Account account : accounts.values()) {
                total += account.getAccountTotal();
            }
            return total;
        }

        public String getUserName() {
            return userName;
        }

        public Map<String, Account> getAccounts() {
            return accounts;
        }

        public void addAccount(String key, Account account) {
            this.accounts.put(key, account);
        }
    }

    public class Account {
        private String accountId;
        private Map<String, Resource> resources;

        public Account(String accountId) {
            this.accountId = accountId;
            this.resources = new HashMap<>();
        }

        public String getAccountId() {
            return accountId;
        }

        public double getAccountDailyTotal(String date) {
            double total = 0.00;
            for (Resource resource : resources.values()) {
                Day day = resource.getDay(date);
                if (day != null) {
                    total += resource.getDay(date).getDailyCost();
                }
            }
            return total;
        }

        public double getAccountTotal() {
            double total = 0;
            for (Resource resource : resources.values()) {
                total += resource.getResourceTotal();
            }
            return total;
        }

        public Map<String, Resource> getResources() {
            return resources;
        }

        public void addResource(String key, Resource resource) {
            this.resources.put(key, resource);
        }
    }

    public class Resource {
        private String productName;
        private Map<String, Day> days;

        public Resource(String productName) {
            this.productName = productName;
            this.days = new HashMap<>();
        }

        public String getResourceName() {
            return productName;
        }

        public double getResourceTotal() {
            double total = 0;
            for (Day day : days.values()) {
                total += day.getDailyCost();
            }
            return total;
        }

        public Map<String, Day> getDays() {
            return days;
        }

        public Day getDay(String key) {
            return days.get(key);
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
