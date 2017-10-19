package services.detailsperuserlast30days;

import db.athena.AthenaClient;
import db.athena.JdbcManager;
import model.Chart;
import services.Service;
import utils.HtmlTableCreator;

import java.util.List;

public class DetailedSpendPerUserLast30DaysDao implements Service{
    private AthenaClient athenaClient;
    private HtmlTableCreator htmlTableCreator;

    public DetailedSpendPerUserLast30DaysDao(AthenaClient athenaClient, HtmlTableCreator htmlTableCreator) {
        this.athenaClient = athenaClient;
        this.htmlTableCreator = htmlTableCreator;
    }

    @Override
    public List<Chart> getCharts() {
        return null;
    }

    public static class DetailedSpendPerUser {

        @JdbcManager.Column(value = "user_owner")
        public String account;
        public String resourceId;
        public String startDate;
        public String cost;
    }
}
