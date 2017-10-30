package loke;

import loke.email.AwsEmailSender;
import loke.email.AwsSesHandler;
import loke.model.Chart;
import loke.model.User;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

public class AwsEmailSenderTest {
    private AwsSesHandler awsSesHandler;
    private AwsEmailSender awsEmailSender;

    @Before
    public void setUp() throws Exception {
        awsSesHandler = Mockito.mock(AwsSesHandler.class);
        awsEmailSender = Mockito.spy(new AwsEmailSender(awsSesHandler, "john@doe.com", "doe.com", false));
    }

    @Test
    public void present_htmlBodyCreation_CreatesExpectedBody() throws Exception {

        List<User> users = new ArrayList<>();
        User user = new User("john.doe");
        Chart chart = new Chart("john.doe");
        chart.addHtmlTable("<table>Table 1</table>");
        chart.addHtmlTable("<table>Table 2</table>");
        user.getCharts().add(chart);
        users.add(user);


        awsEmailSender.present(users);

        String expected = "<table>Table 1</table>\n\n" + "<table>Table 2</table>";

        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(awsSesHandler).sendEmail(Mockito.anyString(), stringArgumentCaptor.capture(), Mockito.anyString(), Mockito.anyString());
        Assert.assertEquals(expected, stringArgumentCaptor.getValue());
    }
}

