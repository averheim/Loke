package loke.services;

import loke.db.athena.AthenaClient;
import loke.db.athena.JdbcManager;
import loke.model.Chart;
import loke.utils.DecimalFormatter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import loke.utils.CalendarGenerator;
import loke.HtmlTableCreator;
import loke.utils.ResourceLoader;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class SpendPerUserAndAccountDao implements Service {
    private AthenaClient athenaClient;
    private HtmlTableCreator htmlTableCreator;
    private String userOwnerRegExp;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static final String SQL_QUERY = ResourceLoader.getResource("sql/CostPerUserByProductAndAccount.sql");
    private static final Logger log = LogManager.getLogger(SpendPerUserAndAccountDao.class);

    public SpendPerUserAndAccountDao(AthenaClient athenaClient, HtmlTableCreator htmlTableCreator, String userOwnerRegExp) {
        this.athenaClient = athenaClient;
        this.htmlTableCreator = htmlTableCreator;
        this.userOwnerRegExp = userOwnerRegExp;
    }

    @Override
    public List<Chart> getCharts() {
        Map<String, User> users = sendRequest();
        return generateCharts(users);
    }

    private List<Chart> generateCharts(Map<String, User> users) {
        List<Chart> charts = new ArrayList<>();
        for (User user : users.values()) {
            Chart chart = new Chart(user.getUserOwner());
            chart.addHtmlTables(generateHTMLTables(user));
            charts.add(chart);
            log.info(chart.getOwner() + "\n" + chart.getHtmlTables());
        }
        return charts;
    }

    private List<String> generateHTMLTables(User user) {
        List<String> htmlTables = new ArrayList<>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM dd, YYYY");
        List<String> head = new ArrayList<>();
        List<List<String>> bodys = new ArrayList<>();
        List<Calendar> calendars = CalendarGenerator.getDaysBack(30);

        head.add("Product Name");
        for (Calendar calendar : calendars) {
            head.add(simpleDateFormat.format(calendar.getTime()));
        }
        head.add("Total");

        for (Account account : user.getAccounts().values()) {
            List<String> accountHeader = new ArrayList<>();
            for (int i = 0; i < 32; i++) {
                accountHeader.add(account.getAccountId());
            }

            List<String> body = new ArrayList<>();
            double total = 0;
            List<Resource> resources = new ArrayList<>();
            resources.addAll(account.getResources().values());
            Collections.reverse(resources);
            for (Resource resource : resources) {
                body.add(resource.productName);
                double resourceTotal = 0;
                for (Calendar calendar : calendars) {
                    Day day = resource.getDays().get(dateFormat.format(calendar.getTime()));
                    if (day != null) {
                        body.add(DecimalFormatter.format(day.getDailyCost(), 2));
                        resourceTotal += day.getDailyCost();
                    } else {
                        body.add("0.00");
                    }
                }
                body.add(DecimalFormatter.format(resourceTotal, 2));
                total += resourceTotal;
            }

            bodys.add(accountHeader);
            bodys.add(body);
            resources.clear();


            String foot = "Total: " + DecimalFormatter.format(total, 2);
            String heading = account.getAccountId();

        }
        htmlTables.add(htmlTableCreator.createTable(head, bodys, null, null));
        return htmlTables;

        /*
        head.add("Product Name");
        for (Calendar calendar : calendars) {
            head.add(simpleDateFormat.format(calendar.getTime()));
        }
        head.add("Total");

        for (Account account : user.getAccounts().values()) {
            List<String> body = new ArrayList<>();

            accountHeader.add(account.getAccountId());

            List<Resource> resources = new ArrayList<>();
            resources.addAll(account.getResources().values());
            Collections.reverse(resources);

            for (Calendar calendar : calendars) {
                double dailyTotal = 0;
                for (Resource resource : resources) {
                    Day day = resource.getDays().get(simpleDateFormat.format(calendar.getTime()));
                    if (day != null) {
                        dailyTotal += day.getDailyCost();
                    }
                    accountHeader.add(DecimalFormatter.format(dailyTotal, 2));
                }
            }
            accountHeader.add("Hej");

            for (Resource resource : resources) {
                body.add(resource.getProductName());
                double resourceTotal = 0;
                for (Calendar calendar : calendars) {
                    Day day = resource.getDays().get(dateFormat.format(calendar.getTime()));
                    body.add(day != null ? DecimalFormatter.format(day.getDailyCost(), 2) : "0.00");
                    resourceTotal += day != null ? day.getDailyCost() : 0.00;
                }
                body.add(DecimalFormatter.format(resourceTotal, 2));
            }

            bodys.add(accountHeader);
            bodys.add(body);

            accountHeader.clear();
            body.clear();
        }
        htmlTables.add(htmlTableCreator.createTable(head, bodys, null, null));

*/
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
