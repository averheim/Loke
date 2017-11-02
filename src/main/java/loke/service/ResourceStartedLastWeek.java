package loke.service;

import loke.HtmlTableCreator;
import loke.db.athena.AthenaClient;
import loke.db.athena.JdbcManager;
import loke.model.Report;
import loke.utils.DecimalFormatter;
import loke.utils.ResourceLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ResourceStartedLastWeek implements Service {
    private static final Logger log = LogManager.getLogger(ResourceStartedLastWeek.class);
    private static final String SQL_QUERY = ResourceLoader.getResource("sql/ResourceStartedLastWeek.sql");
    private AthenaClient athenaClient;
    private HtmlTableCreator htmlTableCreator;
    private String userOwnerRegExp;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public ResourceStartedLastWeek(AthenaClient athenaClient, HtmlTableCreator htmlTableCreator, String userOwnerRegExp) {
        this.athenaClient = athenaClient;
        this.htmlTableCreator = htmlTableCreator;
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
            Report report = new Report(user.getUserOwner());
            report.addHtmlTable(generateHTMLTable(user));
            reports.add(report);
            log.info(report.getOwner() + "\n" + report.getHtmlTables());
        }
        return reports;
    }

    public String generateHTMLTable(User user) {
        List<String> head = new ArrayList<>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM dd, YYYY", Locale.US);
        head.addAll(Arrays.asList("Account", "Product Name", "Resource Id", "Start Date", "Cost ($)"));

        double totalCost = 0;
        List<String> body = new ArrayList<>();
        for (ResourceStartedLastWeekDao dao : user.getResources()) {
            body.add(dao.account);
            body.add(dao.productName);
            body.add(dao.resourceId);
            Calendar day = Calendar.getInstance();
            try {
                day.setTime(dateFormat.parse(dao.startDate));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            body.add(simpleDateFormat.format(day.getTime()));
            body.add(DecimalFormatter.format(dao.cost, 2));
            totalCost += dao.cost;
        }
        String foot = "Total: $" + DecimalFormatter.format(totalCost, 2);
        return htmlTableCreator.createTable(head, body, foot, null, true);
    }

    private Map<String, User> sendRequest() {
        Map<String, User> users = new HashMap<>();
        JdbcManager.QueryResult<ResourceStartedLastWeekDao> queryResult = athenaClient.executeQuery(SQL_QUERY, ResourceStartedLastWeekDao.class);
        for (ResourceStartedLastWeekDao resourceStartedLastWeekDao : queryResult.getResultList()) {
            if (!resourceStartedLastWeekDao.userOwner.matches(userOwnerRegExp)) {
                continue;
            }

            if (!users.containsKey(resourceStartedLastWeekDao.userOwner)) {
                users.put(resourceStartedLastWeekDao.userOwner, new User(resourceStartedLastWeekDao.userOwner));
            }
            users.get(resourceStartedLastWeekDao.userOwner).addResource(resourceStartedLastWeekDao);
        }

        return users;
    }

    public static class ResourceStartedLastWeekDao {
        @JdbcManager.Column(value = "account_name")
        public String account;
        @JdbcManager.Column(value = "user_owner")
        public String userOwner;
        @JdbcManager.Column(value = "product_name")
        public String productName;
        @JdbcManager.Column(value = "resource_id")
        public String resourceId;
        @JdbcManager.Column(value = "start_date")
        public String startDate;
        @JdbcManager.Column(value = "cost")
        public double cost;
    }

    private class User {
        private String userOwner;
        private List<ResourceStartedLastWeekDao> resources;

        public User(String userOwner) {
            this.userOwner = userOwner;
            this.resources = new ArrayList<>();
        }

        public String getUserOwner() {
            return userOwner;
        }

        public List<ResourceStartedLastWeekDao> getResources() {
            return resources;
        }

        public void addResource(ResourceStartedLastWeekDao resource) {
            resources.add(resource);
        }
    }
}
