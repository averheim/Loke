package loke.email;

import loke.model.AdminUser;
import loke.model.Report;
import loke.model.TotalReport;
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

            for (Report report : user.getReports()) {
                for (String htmlURL : report.getHtmlURLs()) {
                    if (user instanceof AdminUser) {
                        if (report instanceof TotalReport) {
                            if (report.getHtmlURLs() != null) {
                                htmlBody.append("<img src=\"").append(htmlURL).append("\"/img>");
                            }
                        }
                    } else {
                        if (report.getHtmlURLs() != null) {
                            htmlBody.append("<img src=\"").append(htmlURL).append("\"/img>");
                        }
                    }
                }
                for (String table : report.getHtmlTables()) {
                    log.info(report.getClass());
                    htmlBody.append(table);
                }
            }
            log.info(htmlBody.toString());

            if (user instanceof AdminUser) {
                awsSesHandler.sendEmail(to, htmlBody.toString().trim(), subject, from);
            }
            if (!dryRun) {
                awsSesHandler.sendEmail(to, htmlBody.toString().trim(), subject, from);
            }
        }
        log.info((dryRun) ? "DryRun: Emails not sent" : "Emails sent");
    }
}
