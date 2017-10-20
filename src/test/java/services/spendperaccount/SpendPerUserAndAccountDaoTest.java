package services.spendperaccount;

import db.athena.AthenaClient;
import org.junit.Before;
import utils.HtmlTableCreator;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static services.spendperaccount.SpendPerUserAndAccountDao.*;

public class SpendPerUserAndAccountDaoTest {
    private AthenaClient athenaClient;
    private HtmlTableCreator htmlTableCreator;
    private SpendPerUserAndAccountDao spendPerUserAndAccountDao;

    @Before
    public void setUp() throws Exception {
        athenaClient = mock(AthenaClient.class);
        htmlTableCreator = new HtmlTableCreator();
        spendPerUserAndAccountDao = new SpendPerUserAndAccountDao(athenaClient, htmlTableCreator);
    }


    private Resource createDbResponse(String userOwner, String accountId, String resourceName, String resourceId, String startDate, double cost) {
        Resource resource = new Resource();
        resource.userOwner = userOwner;
        resource.accountId = accountId;
        resource.resourceName = resourceName;
        resource.resourceId = resourceId;
        resource.startDate = startDate;
        resource.cost = cost;
    }

}

/*
       @JdbcManager.Column(value = "user_owner")
        private String userOwner;
        @JdbcManager.Column(value = "account_id")
        private String accountId;
        @JdbcManager.Column(value = "product_name")
        private String resourceName;
        @JdbcManager.Column(value = "resource_id")
        private String resourceId;
        @JdbcManager.Column(value = "start_date")
        private String startDate;
        @JdbcManager.Column(value = "cost")
        private String cost;
    }
 */