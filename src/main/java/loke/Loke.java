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
import loke.service.SpendPerEmployeeByAccount;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.List;
import java.util.Map;

public class Loke {
    private Configuration configuration;
    private CostReportGenerator costReportGenerator;
    private AwsEmailSender emailSender;
    private AccountReader accountReader;
    private static final Logger logger = LogManager.getLogger(Loke.class);


    public Loke() {
        this.accountReader = new AccountReader();
        Map<String, String> accounts = readAccountsCsv("accounts.csv");
        this.configuration = new YamlReader().readConfigFile("configuration.yaml");
        AthenaClient athenaClient =
                new AthenaClient(
                        configuration.getHost(),
                        configuration.getPort(),
                        configuration.getAccessKey(),
                        configuration.getSecretAccessKey(),
                        configuration.getStagingDir());
        HtmlTableCreator htmlTableCreator = new HtmlTableCreator();
        this.costReportGenerator = new CostReportGenerator(athenaClient, htmlTableCreator, configuration.getUserOwnerRegExp(), configuration.getShowAccountThreshold(), accounts);
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
        logger.info("Loading accounts from: {}", filePath);
        Map<String, String> accounts = null;
        File csv = new File(filePath);
        if (csv.isFile()) {
            try {
                accounts = accountReader.readCSV("accounts.csv");
            } catch (MalformedCSVException e) {
                e.printStackTrace();
            }
        } else {
            logger.info("No resource file found with path: " + filePath);
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
