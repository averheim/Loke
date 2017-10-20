package services.resourcestartedlastweek;

import db.athena.AthenaClient;
import db.athena.JdbcManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import utils.HtmlTableCreator;
import utils.ResourceLoader;
import utils.TestResourceLoader;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static services.resourcestartedlastweek.ResourceStartedLastWeekDao.*;

public class ResourceStartedLastWeekDaoTest {
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
    public void name() throws Exception {
        List<DetailedResource> resultList = new ArrayList<>();
        resultList.add(createUser("QA", "john.doe", "Ec2", "i-01def0a998e06c30e", "2017-09-19", 1000));
        resultList.add(createUser("Nova", "john.doe", "Ec2", "v-01def02344e06c30e", "2017-09-20", 1000));

        JdbcManager.QueryResult queryResult = new JdbcManager.QueryResult();
        queryResult.setResultList(resultList);

        String query = ResourceLoader.getResource("sql/ResourceStartedLastWeek.sql");
        Mockito.when(athenaClient.executeQuery(query, DetailedResource.class)).thenReturn(queryResult);

        String result = resourceStartedLastWeekDao.getCharts().get(0).getHtmlTable();
        String expected = TestResourceLoader.loadResource("DetailedHtmlTableTest1.html");
        assertEquals(expected, result);
    }



    public DetailedResource createUser(String account, String userOwner, String productName, String resourceId, String startDate, double cost) {
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