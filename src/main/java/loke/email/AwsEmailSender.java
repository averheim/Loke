package loke.email;

import loke.model.Chart;
import loke.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class AwsEmailSender implements Presenter {
    private static final Logger log = LogManager.getLogger(AwsEmailSender.class);
    private AwsSesHandler awsSesHandler;
    private String from;
    private String subject = "Test";
    private String toEmailDomainName;
    private boolean dryRun;

    public AwsEmailSender(AwsSesHandler awsSesHandler, String from, String toEmailDomainName, boolean dryRun) {
        this.awsSesHandler = awsSesHandler;
        this.from = from;
        this.toEmailDomainName = toEmailDomainName;
        this.dryRun = dryRun;
    }

    @Override
    public void present(List<User> users) {
        log.info("Sending emails");
        for (User user : users) {
            log.info("Creating email for: {}", user.getUserName());
            String to = user.getUserName() + toEmailDomainName;
            StringBuilder htmlBody = new StringBuilder();

            for (Chart chart : user.getCharts()) {
                if (chart.getHtmlURL() != null) {
                    htmlBody.append("<img src=\"").append(chart.getHtmlURL()).append("\"/img>");
                }
                for (String table : chart.getHtmlTables()) {
                    htmlBody.append(table);
                    htmlBody.append("\n\n");
                }
            }
            log.info(htmlBody.toString());
            if (!dryRun) {
                awsSesHandler.sendEmail(to, htmlBody.toString().trim(), subject, from);
            }
        }
        log.info((dryRun) ? "DryRun: Emails not sent" : "Emails sent");
    }
}
