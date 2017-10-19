package services.detailsperuserlast30days;

import db.athena.AthenaClient;
import db.athena.JdbcManager;
import model.Chart;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import utils.HtmlTableCreator;
import utils.ResourceLoader;
import utils.TestResourceLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static services.detailsperuserlast30days.DetailedSpendPerUserLast30DaysDao.*;

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
        List<DetailedSpendPerUser> resultList = new ArrayList<>();
        resultList.add(createUser("QA", "john.doe", "Ec2", "i-01def0a998e06c30e", "2017-09-19", 1000));
        resultList.add(createUser("Nova", "john.doe", "Ec2", "v-01def02344e06c30e", "2017-09-20", 1000));

        JdbcManager.QueryResult queryResult = new JdbcManager.QueryResult();
        queryResult.setResultList(resultList);

        String query = ResourceLoader.getResource("sql/UserResourceCostDetails.sql");
        Mockito.when(athenaClient.executeQuery(query, DetailedSpendPerUser.class)).thenReturn(queryResult);

        String result = detailedSpendPerUserLast30DaysDao.getCharts().get(0).getHtmlTable();
        String expected = TestResourceLoader.loadResource("DetailedHtmlTableTest1.html");
        assertEquals(expected, result);
    }



    public DetailedSpendPerUser createUser(String account, String userOwner, String productName, String resourceId, String startDate, double cost) {
        DetailedSpendPerUser spendPerUser = new DetailedSpendPerUser();
        spendPerUser.account = account;
        spendPerUser.userOwner = userOwner;
        spendPerUser.productName = productName;
        spendPerUser.resourceId = resourceId;
        spendPerUser.startDate = startDate;
        spendPerUser.cost = cost;
        return spendPerUser;
    }

}