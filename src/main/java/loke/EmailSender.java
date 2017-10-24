package loke;

import java.util.Properties;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailSender {
    public static void main(String[] args) {
        // Recipient's email ID needs to be mentioned.
        String to = "markus.averheim.praktik@widespace.com";

        // Sender's email ID needs to be mentioned
        String from = "markus.averheim.test@gmail.com";
        final String username = "markus.averheim.test@gmail.com";//change accordingly
        final String password = "MarkusTestar";//change accordingly

        // Assuming you are sending email through relay.jangosmtp.net
        String host = "smtp.gmail.com";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", "587");

        // Get the Session object.
        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try {
            // Create a default MimeMessage object.
            Message message = new MimeMessage(session);

            // Set From: header field of the header.
            message.setFrom(new InternetAddress(from));

            // Set To: header field of the header.
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(to));

            // Set Subject: header field
            message.setSubject("Testing Subject");

            // Now set the actual message
            String htmlMessage = "<div style=\"overflow: scroll; width: 30%;\"><table style=\"bordered\"><thead><th>Service</th><th>sep 21, 2017</th><th>sep 22, 2017</th><th>sep 23, 2017</th><th>sep 24, 2017</th><th>sep 25, 2017</th><th>sep 26, 2017</th><th>sep 27, 2017</th><th>sep 28, 2017</th><th>sep 29, 2017</th><th>sep 30, 2017</th><th>okt 01, 2017</th><th>okt 02, 2017</th><th>okt 03, 2017</th><th>okt 04, 2017</th><th>okt 05, 2017</th><th>okt 06, 2017</th><th>okt 07, 2017</th><th>okt 08, 2017</th><th>okt 09, 2017</th><th>okt 10, 2017</th><th>okt 11, 2017</th><th>okt 12, 2017</th><th>okt 13, 2017</th><th>okt 14, 2017</th><th>okt 15, 2017</th><th>okt 16, 2017</th><th>okt 17, 2017</th><th>okt 18, 2017</th><th>okt 19, 2017</th><th>okt 20, 2017</th><th>Total</th></thead><tbody><tr><td>Amazon Elastic Compute Cloud</td><td>35.26</td><td>35</td><td>33.98</td><td>33.65</td><td>34.85</td><td>34.52</td><td>34.92</td><td>35.37</td><td>34.15</td><td>34.09</td><td>00.00</td><td>00.00</td><td>00.00</td><td>00.00</td><td>00.00</td><td>00.00</td><td>00.00</td><td>00.00</td><td>00.00</td><td>00.00</td><td>00.00</td><td>00.00</td><td>00.00</td><td>00.00</td><td>00.00</td><td>00.00</td><td>00.00</td><td>00.00</td><td>00.00</td><td>00.00</td><td>345.73</td></tr><tr><td>Amazon Relational Database Service</td><td>172.46</td><td>172.45</td><td>172.42</td><td>172.42</td><td>172.48</td><td>172.51</td><td>172.47</td><td>172.45</td><td>172.45</td><td>172.42</td><td>00.00</td><td>00.00</td><td>00.00</td><td>00.00</td><td>00.00</td><td>00.00</td><td>00.00</td><td>00.00</td><td>00.00</td><td>00.00</td><td>00.00</td><td>00.00</td><td>00.00</td><td>00.00</td><td>00.00</td><td>00.00</td><td>00.00</td><td>00.00</td><td>00.00</td><td>00.00</td><td>1724.48</td></tr><tr><td>AmazonCloudWatch</td><td>0.23</td><td>0.23</td><td>0.23</td><td>0.23</td><td>0.23</td><td>0.22</td><td>0.22</td><td>0.23</td><td>0.23</td><td>0.23</td><td>00.00</td><td>00.00</td><td>00.00</td><td>00.00</td><td>00.00</td><td>00.00</td><td>00.00</td><td>00.00</td><td>00.00</td><td>00.00</td><td>00.00</td><td>00.00</td><td>00.00</td><td>00.00</td><td>00.00</td><td>00.00</td><td>00.00</td><td>00.00</td><td>00.00</td><td>00.00</td><td>2.25</td></tr></tbody><tfoot><tr><td colspan=\"32\">Total: $2072.45</td></tr></tfoot></table></div>";
            message.setContent(htmlMessage, "text/html");
            // Send message
            Transport.send(message);

            System.out.println("Sent message successfully....");

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
}
