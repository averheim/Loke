package loke;

import loke.config.Configuration;
import loke.config.YamlReader;
import loke.db.athena.AthenaClient;
import loke.model.User;

import java.util.List;

public class Loke {
    private ChartGenerator chartGenerator;

    public Loke() {
        Configuration configuration = new YamlReader().readConfigFile("configuration.yaml");
        AthenaClient athenaClient = new AthenaClient(configuration.getHost(), configuration.getPort(), configuration.getAccessKey(), configuration.getSecretAccessKey(), configuration.getStagingDir());
        HtmlTableCreator htmlTableCreator = new HtmlTableCreator();
        this.chartGenerator = new ChartGenerator(athenaClient, htmlTableCreator);
    }

    public void run() {
        List<User> users = chartGenerator.generateChartsOrderedByUser();
    }
}
