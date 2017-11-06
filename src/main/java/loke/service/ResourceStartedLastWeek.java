package loke.service;

import loke.HtmlTableCreator;
import loke.db.athena.AthenaClient;
import loke.db.athena.JdbcManager;
import loke.model.Report;
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

public class ResourceStartedLastWeek implements Service {
    private static final Logger log = LogManager.getLogger(ResourceStartedLastWeek.class);
    private static final String SQL_QUERY = ResourceLoader.getResource("sql/ResourceStartedLastWeek.sql");
    private AthenaClient athenaClient;
    private HtmlTableCreator htmlTableCreator;
    private String userOwnerRegExp;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private Map<String, String> accounts;

    public ResourceStartedLastWeek(AthenaClient athenaClient, HtmlTableCreator htmlTableCreator, String userOwnerRegExp, Map<String, String> accounts) {
        this.athenaClient = athenaClient;
        this.htmlTableCreator = htmlTableCreator;
        this.userOwnerRegExp = userOwnerRegExp;
        this.accounts = accounts;
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
            report.addHtmlTable(generateHTMLTable(user));
            reports.add(report);
            log.info("Report generated for: {}", user.getUserName());
        }
        return reports;
    }

    private String generateHTMLTable(User user) {
        VelocityEngine velocityEngine = new VelocityEngine();
        velocityEngine.init();

        VelocityContext context = new VelocityContext();
        List<Resource> resources = formatForPresentation(user.getResources());
        double total = 0;
        for (Resource resource : user.getResources()) {
            total += resource.getCost();
        }

        context.put("resources", resources);
        context.put("total", total);

        Template template = velocityEngine.getTemplate("src/templates/resourcesstartedlastweek.vm");

        StringWriter stringWriter = new StringWriter();
        template.merge(context, stringWriter);

        System.out.println(stringWriter);

        for (Resource resource : resources) {
            System.out.printf("%s\n%s\n%s\n%s\n%f\n\n", resource.getAccountId(), resource.getProductName(), resource.getResourceId(), resource.getStartDate(), resource.getCost());
        }
        return stringWriter.toString();
    }

    private List<Resource> formatForPresentation(List<Resource> resources) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM dd, YYYY", Locale.US);
        List<Resource> formattedResources = new ArrayList<>();

        for (Resource resource : resources) {
            String accountId = resource.getAccountId();
            String productName = resource.getProductName();
            String resourceId = resource.getResourceId();
            String startDate = resource.getStartDate();
            double cost = resource.getCost();

            try {
                Calendar date = Calendar.getInstance();
                date.setTime(dateFormat.parse(startDate));
                startDate = simpleDateFormat.format(date.getTime());
                cost = Double.parseDouble(DecimalFormatter.format(cost, 2));

            } catch (ParseException e) {
                e.printStackTrace();
            }

            formattedResources.add(new Resource(
                    accountId,
                    productName,
                    resourceId,
                    startDate,
                    cost)
            );
        }
        return formattedResources;
    }

    private Map<String, User> sendRequest() {
        log.info("Fetching data and mapping objects");
        Map<String, User> users = new HashMap<>();
        JdbcManager.QueryResult<ResourceStartedLastWeekDao> queryResult = athenaClient.executeQuery(SQL_QUERY, ResourceStartedLastWeekDao.class);
        for (ResourceStartedLastWeekDao dao : queryResult.getResultList()) {
            if (!dao.userOwner.matches(userOwnerRegExp)) {
                continue;
            }

            if (!users.containsKey(dao.userOwner)) {
                users.put(dao.userOwner, new User(dao.userOwner));
            }
            users.get(dao.userOwner).addResource(
                    new Resource(dao.accountId, dao.productName, dao.resourceId, dao.startDate, dao.cost
                    ));
        }
        log.info("Done mapping objects");
        return users;
    }

    public static class ResourceStartedLastWeekDao {
        @JdbcManager.Column(value = "account_id")
        public String accountId;
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
        private String userName;
        private List<Resource> resources;

        public User(String userName) {
            this.userName = userName;
            this.resources = new ArrayList<>();
        }

        public String getUserName() {
            return userName;
        }

        public List<Resource> getResources() {
            return resources;
        }

        public void addResource(Resource resource) {
            resources.add(resource);
        }
    }

    public class Resource {
        private String accountId;
        private String productName;
        private String resourceId;
        private String startDate;
        private double cost;

        public Resource(String accountId, String productName, String resourceId, String startDate, double cost) {
            this.accountId = accountId;
            this.productName = productName;
            this.resourceId = resourceId;
            this.startDate = startDate;
            this.cost = cost;
        }

        public String getAccountId() {
            return accountId;
        }

        public String getProductName() {
            return productName;
        }

        public String getResourceId() {
            return resourceId;
        }

        public String getStartDate() {
            return startDate;
        }

        public double getCost() {
            return cost;
        }
    }
}
