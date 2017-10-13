package db;

import db.athena.AthenaClient;

public class SpendPerUserAndResourceDao implements Service {
    private AthenaClient athenaClient;

    public SpendPerUserAndResourceDao(AthenaClient athenaClient) {
        this.athenaClient = athenaClient;
    }

    @Override
    public List<Chart> getCharts() {
        return null;
    }
}
