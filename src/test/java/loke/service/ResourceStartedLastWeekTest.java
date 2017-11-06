package loke.service;

import loke.db.athena.AthenaClient;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import loke.HtmlTableCreator;
import loke.utils.ResourceLoader;
import loke.utils.TestResourceLoader;

import java.util.ArrayList;
import java.util.List;

import static loke.db.athena.JdbcManager.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static loke.service.ResourceStartedLastWeek.*;

public class ResourceStartedLastWeekTest {
    private static final String SQL_QUERY = ResourceLoader.getResource("sql/ResourceStartedLastWeek.sql");
    private AthenaClient athenaClient;
    private HtmlTableCreator htmlTableCreator;
    private ResourceStartedLastWeek resourceStartedLastWeek;

    @Before
    public void setUp() throws Exception {
        athenaClient = mock(AthenaClient.class);
        htmlTableCreator = new HtmlTableCreator();
        String userOwnerRegExp = "";
        resourceStartedLastWeek = new ResourceStartedLastWeek(athenaClient, htmlTableCreator, userOwnerRegExp);
    }

    @Test
    public void test_1() throws Exception {
        List<ResourceStartedLastWeekDao> resultList = new ArrayList<>();
        resultList.add(createDbResponse("QA", "john.doe", "Ec2", "i-01def0a998e06c30e", "2017-09-19", 1000));
        resultList.add(createDbResponse("Nova", "john.doe", "Ec2", "v-01def02344e06c30e", "2017-09-20", 1000));

        QueryResult queryResult = new QueryResult();
        queryResult.setResultList(resultList);

        Mockito.when(athenaClient.executeQuery(SQL_QUERY, ResourceStartedLastWeekDao.class)).thenReturn(queryResult);

        String expected = TestResourceLoader.loadResource("sql/ResourceStartedLastWeekTableTest1.html");
        String result = resourceStartedLastWeek.getReports().get(0).getHtmlTables().get(0);
        assertEquals(expected, result);
    }



    public ResourceStartedLastWeekDao createDbResponse(String account, String userOwner, String productName, String resourceId, String startDate, double cost) {
        ResourceStartedLastWeekDao spendPerUser = new ResourceStartedLastWeekDao();
        spendPerUser.account = account;
        spendPerUser.userOwner = userOwner;
        spendPerUser.productName = productName;
        spendPerUser.resourceId = resourceId;
        spendPerUser.startDate = startDate;
        spendPerUser.cost = cost;
        return spendPerUser;
    }

}