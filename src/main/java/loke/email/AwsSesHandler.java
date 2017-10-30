package loke.email;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AwsSesHandler {
    private static final Logger log = LogManager.getLogger(AwsSesHandler.class);
    private AmazonSimpleEmailService client;

    public AwsSesHandler(AmazonSimpleEmailService client) {
        this.client = client;
    }

    public void sendEmail(String to, String htmlBody, String subject, String from) {
        try {
            SendEmailRequest request = new SendEmailRequest()
                    .withDestination(new Destination().withToAddresses(to))
                    .withMessage(new Message()
                            .withBody(new Body().withHtml(new Content().withCharset("UTF-8").withData(htmlBody)))
                            .withSubject(new Content().withCharset("UTF-8").withData(subject)))
                    .withSource(from);
            client.sendEmail(request);
            log.info("Email sent!");
        } catch (Exception ex) {
            log.error("The email was not sent. Error message: "
                    + ex.getMessage());
        }
    }
}
