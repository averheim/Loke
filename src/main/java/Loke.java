import db.Service;
import db.SpendPerUserAndResourceDao;
import db.athena.AthenaClient;

public class Loke {
    private AthenaClient athenaClient;
    private Service spendPerUserAndResourceDao;

    public Loke() {
        this.athenaClient = new AthenaClient("athena-eu-west-1.amazonaws.com",
                443,
                "AKIAJXBQ66SAW4EAR3DQ",
                "2X5w24XnkbUc+VINz3WJ7549mPHXu22y1WP7aJJn",
                "s3://wsbillingreports");
        this.spendPerUserAndResourceDao = new SpendPerUserAndResourceDao(athenaClient);
    }

    public void run() {
        spendPerUserAndResourceDao.getCharts();
    }
}
