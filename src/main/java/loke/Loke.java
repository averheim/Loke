package loke;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import loke.config.Configuration;
import loke.config.YamlReader;
import loke.db.athena.AthenaClient;
import loke.email.AwsEmailSender;
import loke.email.AwsSesHandler;
import loke.email.Presenter;
import loke.model.Chart;
import loke.model.User;

import java.util.ArrayList;
import java.util.List;

public class Loke {
    private ChartGenerator chartGenerator;
    private Presenter presenter;

    public Loke() {
        Configuration configuration = new YamlReader().readConfigFile("configuration.yaml");
        AthenaClient athenaClient =
                new AthenaClient(
                        configuration.getHost(),
                        configuration.getPort(),
                        configuration.getAccessKey(),
                        configuration.getSecretAccessKey(),
                        configuration.getStagingDir());
        HtmlTableCreator htmlTableCreator = new HtmlTableCreator();
        this.chartGenerator = new ChartGenerator(athenaClient, htmlTableCreator, configuration.getUserOwnerRegExp(), configuration.getShowAccountThreshold());
        AwsSesHandler awsSesHandler = new AwsSesHandler(AmazonSimpleEmailServiceClientBuilder.standard()
                .withRegion(configuration.getRegion())
                .withCredentials(
                        new AWSStaticCredentialsProvider(new BasicAWSCredentials(
                                configuration.getAccessKey(),
                                configuration.getSecretAccessKey())))
                .build());
        this.presenter = new AwsEmailSender(
                awsSesHandler,
                configuration.getFromEmailAddress(),
                configuration.getToEmailDomainName(),
                configuration.isDryRun());
    }

    public void run() {
        List<User> admins = new ArrayList<>();
        admins.add(new User("christopher.olsson.praktik"));
        chartGenerator.addAdmins(admins);
        List<User> users = chartGenerator.generateChartsOrderedByUser();
        for (User user : users) {
            if (user.getUserName().equals("christopher.olsson.praktik")) {
                System.out.println("ADMIN");
                System.out.println(user.getCharts().size());
                for (Chart chart : user.getCharts()) {
                    System.out.println(chart.getHtmlURL());
                    System.out.println(chart.getHtmlTables());
                }
            }
        }
    }
    // presenter.present(users);
}
