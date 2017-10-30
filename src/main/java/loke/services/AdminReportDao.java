package loke.services;

import loke.HtmlTableCreator;
import loke.db.athena.AthenaClient;
import loke.db.athena.JdbcManager;
import loke.model.Chart;
import loke.utils.ResourceLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminReportDao implements Service {
    private static final Logger log = LogManager.getLogger(AdminReportDao.class);
    private AthenaClient athenaClient;
    private HtmlTableCreator htmlTableCreator;
    private String userOwnerRegExp;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static final String SQL_QUERY = ResourceLoader.getResource("sql/CostPerUserByProductAndAccount.sql");

    public AdminReportDao(AthenaClient athenaClient, HtmlTableCreator htmlTableCreator, String userOwnerRegExp) {
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
        return null;
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
        @JdbcManager.Column(value = "linked_account_id")
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
