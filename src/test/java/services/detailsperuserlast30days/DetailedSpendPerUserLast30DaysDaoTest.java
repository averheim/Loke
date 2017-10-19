package services.detailsperuserlast30days;

import db.athena.AthenaClient;
import org.junit.Before;
import org.junit.Test;
import utils.HtmlTableCreator;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;

public class DetailedSpendPerUserLast30DaysDaoTest {
    private AthenaClient athenaClient;
    private HtmlTableCreator htmlTableCreator;
    private DetailedSpendPerUserLast30DaysDao detailedSpendPerUserLast30DaysDao;

    @Before
    public void setUp() throws Exception {
        athenaClient = mock(AthenaClient.class);
        htmlTableCreator = new HtmlTableCreator();
        detailedSpendPerUserLast30DaysDao = new DetailedSpendPerUserLast30DaysDao(athenaClient, htmlTableCreator);
    }

    @Test
    public void name() throws Exception {
        List<DetailedSpendPerUserLast30DaysDao.DetailedSpendPerUser> result = new ArrayList<>();
        DetailedSpendPerUserLast30DaysDao.DetailedSpendPerUser spendPerUser = new DetailedSpendPerUserLast30DaysDao.DetailedSpendPerUser();

        spendPerUser.account = "123";
        spendPerUser.resourceId = "i-instance-id";
        spendPerUser.startDate = "2017-09-20";
        spendPerUser.cost = "1000";
    }

}