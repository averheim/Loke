package loke;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import loke.config.AccountReader;
import loke.config.Configuration;
import loke.config.MalformedCSVException;
import loke.config.YamlReader;
import loke.db.athena.AthenaClient;
import loke.email.AwsEmailSender;
import loke.email.AwsSesHandler;
import loke.model.Employee;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.velocity.app.VelocityEngine;

import java.io.File;
import java.util.List;
import java.util.Map;

public class Loke {
    private static final Logger log = LogManager.getLogger(Loke.class);
    private AccountReader accountReader;
    private Configuration configuration;
    private CostReportGenerator costReportGenerator;
    private AwsEmailSender emailSender;


    public Loke() {
        this.accountReader = new AccountReader();
        Map<String, String> csvAccounts = readAccountsCsv("accounts.csv");
        this.configuration = new YamlReader().readConfigFile("configuration.yaml");

        AthenaClient athenaClient = new AthenaClient(
                configuration.getHost(),
                configuration.getPort(),
                configuration.getAccessKey(),
                configuration.getSecretAccessKey(),
                configuration.getStagingDir());

        HtmlTableCreator htmlTableCreator = new HtmlTableCreator();

        this.costReportGenerator = new CostReportGenerator(athenaClient,
                htmlTableCreator,
                configuration.getUserOwnerRegExp(),
                configuration.getShowAccountThreshold(),
                csvAccounts,
                new VelocityEngine());

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

    private Map<String, String> readAccountsCsv(String filePath) {
        log.info("Loading accounts from: {}", filePath);
        Map<String, String> accounts = null;
        File csv = new File(filePath);
        if (csv.isFile()) {
            try {
                accounts = accountReader.readCSV("accounts.csv");
            } catch (MalformedCSVException e) {
                e.printStackTrace();
            }
        } else {
            log.info("No resource file found with path: " + filePath);
        }
        return accounts;
    }

    public void run() {
        List<Employee> employeeReports = costReportGenerator.generateReports();
        List<Employee> adminReports = costReportGenerator.generateAdminReports();
        emailSender.sendEmployeeMails(employeeReports);
        emailSender.sendAdminMails(configuration.getAdmins(), adminReports);
    }
}
