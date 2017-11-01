package loke.email;

import loke.model.Admin;
import loke.model.Employee;
import loke.model.Report;
import loke.model.TotalReport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class AwsEmailSender {
    private static final Logger log = LogManager.getLogger(AwsEmailSender.class);
    private AwsSesHandler awsSesHandler;
    private String toEmailDomainName;
    private String subject = "Weekly AWS cost report";
    private String from;
    private boolean dryRun;

    public AwsEmailSender(AwsSesHandler awsSesHandler, String from, String toEmailDomainName, boolean dryRun) {
        this.awsSesHandler = awsSesHandler;
        this.from = from;
        this.toEmailDomainName = toEmailDomainName;
        this.dryRun = dryRun;
    }

    public void sendEmployeeMail(List<Employee> employeeReports) {
        log.info("Sending emails");
        for (Employee employee : employeeReports) {
            StringBuilder htmlBody = new StringBuilder();
            log.info("Creating email for: {}", employee.getUserName());
            String to = employee.getUserName() + toEmailDomainName;
            for (Report report : employee.getReports()) {
                addHtmlURLs(htmlBody, report);
                addHtmlTables(htmlBody, report);
            }
            log.info(htmlBody.toString());
            if (!dryRun) {
                awsSesHandler.sendEmail(to, htmlBody.toString().trim(), subject, from);
            } else {
                log.info("DryRun: Emails not sent\nMail: {}", htmlBody.toString().trim());
            }
        }
    }

    public void sendAdminMail(List<Admin> admins, List<Employee> adminReports) {
        log.info("Sending emails");
        StringBuilder htmlBody = new StringBuilder();
        for (Employee employee : adminReports) {
            log.info("Creating email for: {}", employee.getUserName());
            for (Report report : employee.getReports()) {
                if (report instanceof TotalReport) {
                    addHtmlURLs(htmlBody, report);
                }
                addHtmlTables(htmlBody, report);
            }
            log.info(htmlBody.toString());
        }
        if (!dryRun) {
            for (Admin admin : admins) {
                awsSesHandler.sendEmail(admin.getEmailAddress(), htmlBody.toString().trim(), subject, from);
            }
        } else {
            log.info("DryRun: Emails not sent\nMail: {}", htmlBody.toString().trim());
        }
    }

    private void addHtmlURLs(StringBuilder htmlBody, Report report) {
        for (String htmlURL : report.getHtmlURLs()) {
            if (report.getHtmlURLs() != null) {
                htmlBody.append("<img src=\"").append(htmlURL).append("\"/img>");
                htmlBody.append("\n\n");
            }
        }
    }

    private void addHtmlTables(StringBuilder htmlBody, Report report) {
        for (String table : report.getHtmlTables()) {
            htmlBody.append(table);
            htmlBody.append("\n\n");
        }
    }
}
