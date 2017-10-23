package services;

import db.athena.AthenaClient;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import services.ResourceStartedLastWeekDao;
import utils.HtmlTableCreator;
import utils.ResourceLoader;
import utils.TestResourceLoader;

import java.util.ArrayList;
import java.util.List;

import static db.athena.JdbcManager.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static services.ResourceStartedLastWeekDao.*;

public class ResourceStartedLastWeekDaoTest {
    private static final String SQL_QUERY = ResourceLoader.getResource("sql/ResourceStartedLastWeek.sql");
    private AthenaClient athenaClient;
    private HtmlTableCreator htmlTableCreator;
    private ResourceStartedLastWeekDao resourceStartedLastWeekDao;

    @Before
    public void setUp() throws Exception {
        athenaClient = mock(AthenaClient.class);
        htmlTableCreator = new HtmlTableCreator();
        resourceStartedLastWeekDao = new ResourceStartedLastWeekDao(athenaClient, htmlTableCreator);
    }

    @Test
    public void test_1() throws Exception {
        List<DetailedResource> resultList = new ArrayList<>();
        resultList.add(createDbResponse("QA", "john.doe", "Ec2", "i-01def0a998e06c30e", "2017-09-19", 1000));
        resultList.add(createDbResponse("Nova", "john.doe", "Ec2", "v-01def02344e06c30e", "2017-09-20", 1000));

        QueryResult queryResult = new QueryResult();
        queryResult.setResultList(resultList);

        Mockito.when(athenaClient.executeQuery(SQL_QUERY, DetailedResource.class)).thenReturn(queryResult);

        String expected = TestResourceLoader.loadResource("ResourceStartedLastWeekTableTest1.html");
        String result = resourceStartedLastWeekDao.getCharts().get(0).getHtmlTables().get(0);
        assertEquals(expected, result);
    }



    public DetailedResource createDbResponse(String account, String userOwner, String productName, String resourceId, String startDate, double cost) {
        DetailedResource spendPerUser = new DetailedResource();
        spendPerUser.account = account;
        spendPerUser.userOwner = userOwner;
        spendPerUser.productName = productName;
        spendPerUser.resourceId = resourceId;
        spendPerUser.startDate = startDate;
        spendPerUser.cost = cost;
        return spendPerUser;
    }

}