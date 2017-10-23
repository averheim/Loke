package services.spendperaccount;

import db.athena.AthenaClient;
import org.junit.Before;
import org.junit.Test;
import utils.CalendarGenerator;
import utils.HtmlTableCreator;
import utils.ResourceLoader;
import utils.TestResourceLoader;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static db.athena.JdbcManager.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static services.spendperaccount.SpendPerUserAndAccountDao.*;

public class SpendPerUserAndAccountDaoTest {
    private static final String SQL_QUERY = ResourceLoader.getResource("sql/CostPerUserByProductAndAccount.sql");
    private AthenaClient athenaClient;
    private HtmlTableCreator htmlTableCreator;
    private SpendPerUserAndAccountDao spendPerUserAndAccountDao;
    private Clock clock;

    @Before
    public void setUp() throws Exception {
        clock = mock(Clock.class);
        CalendarGenerator.clock = clock;
        athenaClient = mock(AthenaClient.class);
        htmlTableCreator = new HtmlTableCreator();
        spendPerUserAndAccountDao = new SpendPerUserAndAccountDao(athenaClient, htmlTableCreator);
    }

    @Test
    public void test_1() throws Exception {
        List<SpendPerUserAndAccount> spendPerUserAndAccounts = new ArrayList<>();
        spendPerUserAndAccounts.add(createDbResponse("john.doe", "QA", "Ec2", "2017-09-01 09:00:00", 100));
        spendPerUserAndAccounts.add(createDbResponse("john.doe", "QA", "Ec2", "2017-09-02 09:00:00", 100));
        spendPerUserAndAccounts.add(createDbResponse("john.doe", "QA", "Ec2", "2017-09-03 09:00:00", 50));
        spendPerUserAndAccounts.add(createDbResponse("john.doe", "QA", "S3", "2017-09-01 09:00:00", 300));
        spendPerUserAndAccounts.add(createDbResponse("john.doe", "Nova", "S3", "2017-09-11 10:00:00", 100));
        QueryResult queryResult = new QueryResult();
        queryResult.setResultList(spendPerUserAndAccounts);

        when(clock.instant()).thenReturn(Instant.parse("2017-09-30T00:00:00Z"));
        when(athenaClient.executeQuery(SQL_QUERY, SpendPerUserAndAccount.class)).thenReturn(queryResult);

        String expected = TestResourceLoader.loadResource("SpendPerUserAndAccountTableTest1.html");
        String result = spendPerUserAndAccountDao.getCharts().get(0).getHtmlTables().get(0);
        System.out.println(spendPerUserAndAccountDao.getCharts().get(0).getHtmlTables().get(1));
        assertEquals(expected, result);
    }

    private SpendPerUserAndAccount createDbResponse(String userOwner, String accountId, String productName,
                                                    String startDate, double cost) {
        SpendPerUserAndAccount spendPerUserAndAccount = new SpendPerUserAndAccount();
        spendPerUserAndAccount.userOwner = userOwner;
        spendPerUserAndAccount.accountId = accountId;
        spendPerUserAndAccount.productName = productName;
        spendPerUserAndAccount.startDate = startDate;
        spendPerUserAndAccount.cost = cost;
        return spendPerUserAndAccount;
    }

}