package loke;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.*;
import loke.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class AwsEmailSender implements Presenter {
    private static final Logger log = LogManager.getLogger(AwsEmailSender.class);
    private AmazonSimpleEmailService client;
    private String from;
    private String to = "christopher.olsson.praktik@widespace.com";
    private String subject = "Test";
    private String htmlBody = "<h1>Amazon SES test (AWS SDK for Java)</h1>"
            + "<p>This email was sent with <a href='https://aws.amazon.com/ses/'>"
            + "Amazon SES</a> using the <a href='https://aws.amazon.com/sdk-for-java/'>"
            + "AWS SDK for Java</a>";

    public AwsEmailSender(String from, String accessKey, String secretAccessKey, String region) {
        this.from = from;
        setupAwsClient(accessKey, secretAccessKey, region);
    }

    private void setupAwsClient(String accessKey, String secretAccessKey, String region) {
        BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretAccessKey);
        client = AmazonSimpleEmailServiceClientBuilder.standard()
                .withRegion(region)
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .build();
    }

    @Override
    public void present(List<User> users) {
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
