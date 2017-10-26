package loke;

import loke.config.Configuration;
import loke.config.YamlReader;
import loke.db.athena.AthenaClient;
import loke.model.User;

import java.util.ArrayList;
import java.util.List;

public class Loke {
    private ChartGenerator chartGenerator;
    private Presenter presenter;

    public Loke() {
        Configuration configuration = new YamlReader().readConfigFile("configuration.yaml");
        AthenaClient athenaClient = new AthenaClient(configuration.getHost(), configuration.getPort(), configuration.getAccessKey(), configuration.getSecretAccessKey(), configuration.getStagingDir());
        HtmlTableCreator htmlTableCreator = new HtmlTableCreator();
        this.chartGenerator = new ChartGenerator(athenaClient, htmlTableCreator, configuration.getUserOwnerRegExp());
        this.presenter = new AwsEmailSender(
                configuration.getEmailFrom(),
                configuration.getAccessKey(),
                configuration.getSecretAccessKey(),
                configuration.getRegion());
    }

    public void run() {
        List<User> users = chartGenerator.generateChartsOrderedByUser();
        presenter.present(new ArrayList<>());
    }
}
