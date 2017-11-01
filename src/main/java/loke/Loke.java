package loke;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import loke.config.Configuration;
import loke.config.YamlReader;
import loke.db.athena.AthenaClient;
import loke.email.AwsEmailSender;
import loke.email.AwsSesHandler;
import loke.model.Employee;

import java.util.List;

public class Loke {
    private Configuration configuration;
    private CostReportGenerator costReportGenerator;
    private AwsEmailSender emailSender;

    public Loke() {
        this.configuration = new YamlReader().readConfigFile("configuration.yaml");
        AthenaClient athenaClient =
                new AthenaClient(
                        configuration.getHost(),
                        configuration.getPort(),
                        configuration.getAccessKey(),
                        configuration.getSecretAccessKey(),
                        configuration.getStagingDir());
        HtmlTableCreator htmlTableCreator = new HtmlTableCreator();
        this.costReportGenerator = new CostReportGenerator(athenaClient, htmlTableCreator, configuration.getUserOwnerRegExp(), configuration.getShowAccountThreshold());
        AwsSesHandler awsSesHandler = new AwsSesHandler(AmazonSimpleEmailServiceClientBuilder.standard()
                .withRegion(configuration.getRegion())
                .withCredentials(
                        new AWSStaticCredentialsProvider(new BasicAWSCredentials(
                                configuration.getAccessKey(),
                                configuration.getSecretAccessKey())))
                .build());
        this.emailSender = new AwsEmailSender(
                awsSesHandler,
                configuration.getFromEmailAddress(),
                configuration.getToEmailDomainName(),
                configuration.isDryRun());
    }

    public void run() {
        //List<Employee> employeeReports = costReportGenerator.generateReports();
        List<Employee> adminReports = costReportGenerator.generateAdminReports();
        //emailSender.sendEmployeeMails(employeeReports);
        emailSender.sendAdminMails(configuration.getAdmins(), adminReports);
    }
}
