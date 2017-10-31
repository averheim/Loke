package loke.service;

import loke.db.athena.AthenaClient;
import loke.db.athena.JdbcManager;
import loke.model.Report;
import loke.utils.DecimalFormatter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import loke.utils.CalendarGenerator;
import loke.HtmlTableCreator;
import loke.utils.ResourceLoader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class SpendPerUserAndAccountDao implements Service {
    private static final Logger log = LogManager.getLogger(SpendPerUserAndAccountDao.class);
    private AthenaClient athenaClient;
    private HtmlTableCreator htmlTableCreator;
    private String userOwnerRegExp;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static final String SQL_QUERY = ResourceLoader.getResource("sql/CostPerUserByProductAndAccount.sql");
    private double showAccountThreshold;

    public SpendPerUserAndAccountDao(AthenaClient athenaClient, HtmlTableCreator htmlTableCreator, String userOwnerRegExp, double showAccountThreshold) {
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
        List<Report> reports = new ArrayList<>();
        for (User user : users.values()) {
            Report report = new Report(user.getUserOwner());
            report.addHtmlTables(generateHTMLTables(user));
            reports.add(report);
            log.info(report.getOwner() + "\n" + report.getHtmlTables());
        }
        return reports;
    }

    private List<String> generateHTMLTables(User user) {
        List<String> htmlTables = new ArrayList<>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM dd, YYYY", Locale.US);
        List<String> head = new ArrayList<>();
        List<Calendar> calendarDaysBack = CalendarGenerator.getDaysBack(30);

        head.add("Products by Account");
        for (Calendar calendar : calendarDaysBack) {
            head.add(simpleDateFormat.format(calendar.getTime()));
        }
        head.add("Total ($)");

        List<List<String>> bodies = new ArrayList<>();
        for (Account account : user.getAccounts().values()) {
            List<Resource> resources = new ArrayList<>(account.getResources().values());
            Collections.reverse(resources);

            // Check if account should be shown
            List<String> accountTotalRows = getAccountTotalRows(calendarDaysBack, account, resources);
            double accountTotal = Double.valueOf(accountTotalRows.get(accountTotalRows.size() - 1));

            if (accountTotal >= showAccountThreshold) {
                bodies.add(getAccountTotalRows(calendarDaysBack, account, resources));
                bodies.add(getResourceRows(calendarDaysBack, resources));
            }
            resources.clear();
        }
        bodies.add(getTotalCostRow(user, calendarDaysBack));

        List<String> total = getTotalCostRow(user, calendarDaysBack);
        double footerTotal = Double.valueOf(total.get(total.size() - 1));
        String heading = "Monthly account details (Accounts with total cost below $"
                + DecimalFormatter.format(showAccountThreshold, 2) + " will not be shown)";
        String footer = "Monthly total: $" + footerTotal;

        if (bodies.size() > 1) {
            htmlTables.add(htmlTableCreator.createMarkedRowTable(head, bodies, footer, heading, "Total"));
        }

        return htmlTables;
    }

    private List<String> getAccountTotalRows(List<Calendar> calendarDaysBack, Account account, List<Resource> resources) {
        List<String> accountRows = new ArrayList<>();
        accountRows.add(account.getAccountId() + " Total ($)");

        double accountTotal = 0;
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

        double ultimateTotal = 0;
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
            ultimateTotal += dailyTotal;
            totalCostRows.add((dailyTotal != 0) ? DecimalFormatter.format(dailyTotal, 2) : "0.00");
        }
        totalCostRows.add((ultimateTotal != 0) ? DecimalFormatter.format(ultimateTotal, 2) : "0.00");
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
