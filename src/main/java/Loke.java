import model.User;
import services.*;
import db.athena.AthenaClient;
import utils.HtmlTableCreator;

import java.util.List;

public class Loke {
    private ChartGenerator chartGenerator;
    public Loke() {
        AthenaClient athenaClient = new AthenaClient("athena.eu-west-1.amazonaws.com",
                443,
                "AKIAJXBQ66SAW4EAR3DQ",
                "2X5w24XnkbUc+VINz3WJ7549mPHXu22y1WP7aJJn",
                "s3://wsqa-billingreports");
        HtmlTableCreator htmlTableCreator = new HtmlTableCreator();
        this.chartGenerator = new ChartGenerator(athenaClient, htmlTableCreator);
    }

    public void run() {
        List<User> users = chartGenerator.generateChartsOrderedByUser();
    }
}
