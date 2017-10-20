import services.Service;
import services.resourcestartedlastweek.ResourceStartedLastWeekDao;
import services.spendperuserlast30days.SpendPerUserLast30DaysDao;
import db.athena.AthenaClient;
import utils.HtmlTableCreator;

public class Loke {
    private Service spendPerUserAndResourceDao;
    private Service resourceStartedLastWeekDao;

    public Loke() {
        AthenaClient athenaClient = new AthenaClient("athena.eu-west-1.amazonaws.com",
                443,
                "AKIAJXBQ66SAW4EAR3DQ",
                "2X5w24XnkbUc+VINz3WJ7549mPHXu22y1WP7aJJn",
                "s3://wsbillingreports");
        HtmlTableCreator htmlTableCreator = new HtmlTableCreator();
        this.spendPerUserAndResourceDao = new SpendPerUserLast30DaysDao(athenaClient, htmlTableCreator);
        this.resourceStartedLastWeekDao = new ResourceStartedLastWeekDao(athenaClient, htmlTableCreator);
    }

    public void run() {
        //spendPerUserAndResourceDao.getCharts();
        resourceStartedLastWeekDao.getCharts();
    }
}
