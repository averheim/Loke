package loke.service;

import loke.config.AccountReader;
import loke.db.athena.AthenaClient;
import org.apache.velocity.app.VelocityEngine;
import org.junit.Before;
import org.junit.Test;
import loke.utils.CalendarGenerator;
import loke.HtmlTableCreator;
import loke.utils.ResourceLoader;
import loke.utils.ResourceLoaderTestUtility;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static loke.db.athena.JdbcManager.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static loke.service.SpendPerEmployeeByAccount.*;

public class SpendPerEmployeeByAccountDaoTest {
    private static final String SQL_QUERY = ResourceLoader.getResource("sql/SpendPerEmployeeByAccount.sql");
    private AthenaClient athenaClient;
    private HtmlTableCreator htmlTableCreator;
    private SpendPerEmployeeByAccount spendPerEmployeeByAccount;
    private Clock clock;

    @Before
    public void setUp() throws Exception {
        clock = mock(Clock.class);
        CalendarGenerator.clock = clock;
        athenaClient = mock(AthenaClient.class);
        htmlTableCreator = new HtmlTableCreator();
        String userOwnerRegExp = "";
        Map<String, String> accounts = new AccountReader().readCSV("config/accounts.csv");
        spendPerEmployeeByAccount = new SpendPerEmployeeByAccount(athenaClient, userOwnerRegExp, 0, accounts, new VelocityEngine());
    }

    @Test
    public void test_1() throws Exception {
        List<SpendPerEmployeeAndAccountDao> spendPerEmployeeAndAccountDaos = new ArrayList<>();
        spendPerEmployeeAndAccountDaos.add(createDbResponse("john.doe", "QA", "Ec2", "2017-09-01 09:00:00", 100));
        spendPerEmployeeAndAccountDaos.add(createDbResponse("john.doe", "QA", "Ec2", "2017-09-02 09:00:00", 100));
        spendPerEmployeeAndAccountDaos.add(createDbResponse("john.doe", "QA", "Ec2", "2017-09-03 09:00:00", 50));
        spendPerEmployeeAndAccountDaos.add(createDbResponse("john.doe", "QA", "S3", "2017-09-01 09:00:00", 300));
        spendPerEmployeeAndAccountDaos.add(createDbResponse("john.doe", "Nova", "S3", "2017-09-11 10:00:00", 100));
        QueryResult queryResult = new QueryResult();
        queryResult.setResultList(spendPerEmployeeAndAccountDaos);

        when(clock.instant()).thenReturn(Instant.parse("2017-09-30T00:00:00Z"));
        when(athenaClient.executeQuery(SQL_QUERY, SpendPerEmployeeAndAccountDao.class)).thenReturn(queryResult);

        String expected = ResourceLoaderTestUtility.loadResource("sql/SpendPerUserAndAccountTableTest1.html");
        String result = spendPerEmployeeByAccount.getReports().get(0).getHtmlTables().get(0);
        System.out.println(spendPerEmployeeByAccount.getReports().get(0).getHtmlTables().get(1));
        assertEquals(expected, result);
    }

    private SpendPerEmployeeAndAccountDao createDbResponse(String userOwner, String accountId, String productName,
                                                           String startDate, double cost) {
        SpendPerEmployeeAndAccountDao spendPerEmployeeAndAccountDao = new SpendPerEmployeeAndAccountDao();
        spendPerEmployeeAndAccountDao.userOwner = userOwner;
        spendPerEmployeeAndAccountDao.accountId = accountId;
        spendPerEmployeeAndAccountDao.productName = productName;
        spendPerEmployeeAndAccountDao.startDate = startDate;
        spendPerEmployeeAndAccountDao.cost = cost;
        return spendPerEmployeeAndAccountDao;
    }

}