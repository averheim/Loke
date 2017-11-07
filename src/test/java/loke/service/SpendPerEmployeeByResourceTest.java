package loke.service;

import loke.HtmlTableCreator;
import loke.db.athena.AthenaClient;
import loke.db.athena.JdbcManager.*;
import loke.utils.CalendarGenerator;
import loke.utils.ResourceLoader;
import loke.utils.TestResourceLoader;
import org.apache.velocity.app.VelocityEngine;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static loke.service.SpendPerEmployeeByResource.SpendPerEmployeeByResourceDao;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class SpendPerEmployeeByResourceTest {

    private static final String SQL_QUERY = ResourceLoader.getResource("sql/SpendPerEmployeeByResource.sql");
    private AthenaClient athenaClient;
    private HtmlTableCreator htmlTableCreator;
    private SpendPerEmployeeByResource spendPerEmployeeByResource;

    @Before
    public void setUp() throws Exception {
        athenaClient = mock(AthenaClient.class);
        htmlTableCreator = new HtmlTableCreator();
        String userOwnerRegExp = "john.doe";
        spendPerEmployeeByResource = new SpendPerEmployeeByResource(athenaClient, userOwnerRegExp, new VelocityEngine());
    }

    @Test
    public void test_1() throws Exception {
        List<SpendPerEmployeeByResourceDao> resultList = new ArrayList<>();
        resultList.add(createDbResponse("john.doe", "S3", "2017-11-07", 200));
        resultList.add(createDbResponse("john.doe", "EC2", "2017-11-07", 100));
        resultList.add(createDbResponse("john.doe", "Elastic Map Reduce", "2017-11-05", 20000));
        resultList.add(createDbResponse("john.doe", "Loadbalancers", "2017-11-05", 1000));

        QueryResult queryResult = new QueryResult();
        queryResult.setResultList(resultList);

        Mockito.when(athenaClient.executeQuery(SQL_QUERY, SpendPerEmployeeByResourceDao.class)).thenReturn(queryResult);

        String expected = TestResourceLoader.loadResource("sql/ResourceStartedLastWeekTableTest1.html");
        String result = spendPerEmployeeByResource.getReports().get(0).getHtmlTables().get(0);
        assertEquals(expected, result);
    }

    public SpendPerEmployeeByResourceDao createDbResponse(String userOwner, String productName, String startDate, double cost) {
        SpendPerEmployeeByResourceDao spendPerUser = new SpendPerEmployeeByResourceDao();
        spendPerUser.userOwner = userOwner;
        spendPerUser.productName = productName;
        spendPerUser.startDate = startDate;
        spendPerUser.cost = cost;
        return spendPerUser;
    }
}