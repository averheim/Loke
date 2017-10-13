package db;

import db.athena.AthenaClient;
import db.athena.JdbcManager;
import model.Chart;

import java.util.List;

public class SpendPerUserAndResourceDao implements Service {
    private AthenaClient athenaClient;
    private static final String SQL_QUERY = ResourceLoader.getResource("sql/CostPerUserAndProductLast30Days.sql");

    public SpendPerUserAndResourceDao(AthenaClient athenaClient) {
        this.athenaClient = athenaClient;
    }

    @Override
    public List<Chart> getCharts() {
        sendRequest();
        return null;
    }

    private void sendRequest() {
        JdbcManager.QueryResult<SpendPerUserAndResource> spendPerUserAndResourceQueryResult = athenaClient.executeQuery(SQL_QUERY, SpendPerUserAndResource.class);
        for (SpendPerUserAndResource spendPerUserAndResource : spendPerUserAndResourceQueryResult.getResultList()) {
            System.out.println(spendPerUserAndResource.userOwner);
            System.out.println(spendPerUserAndResource.productName);
            System.out.println(spendPerUserAndResource.cost);
            System.out.println(spendPerUserAndResource.startDate);
        }
    }

    public static class SpendPerUserAndResource{
        @JdbcManager.Column(value = "user_owner")
        public String userOwner;
        @JdbcManager.Column(value = "product_name")
        public String productName;
        @JdbcManager.Column(value = "cost")
        public double cost;
        @JdbcManager.Column(value = "start_date")
        public String startDate;
    }
}
