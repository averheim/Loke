import db.Service;
import db.SpendPerUserAndResourceDao;
import db.athena.AthenaClient;

public class Loke {
    private AthenaClient athenaClient;
    private Service SpendPerUserAndResourceDao;

    public Loke() {
        this.athenaClient = new AthenaClient("athena-eu-west-1.amazonaws.com",
                null,
                "",
                "",
                "s3://wsbillingreports");
        this.SpendPerUserAndResourceDao = new SpendPerUserAndResourceDao(athenaClient);
    }

    public void run() {
    }
}
