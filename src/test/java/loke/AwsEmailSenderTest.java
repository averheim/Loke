package loke;

import loke.email.AwsEmailSender;
import loke.email.AwsSesHandler;
import loke.model.Employee;
import loke.model.Report;
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

        List<Employee> employees = new ArrayList<>();
        Employee employee = new Employee("john.doe");
        Report reportOne = new Report("john.doe");
        Report reportTwo = new Report("john.doe");
        reportOne.setHtmlTable("<table>Table 1</table>");
        reportTwo.setHtmlTable("<table>Table 2</table>");

        employee.addReport(reportOne);
        employee.addReport(reportTwo);
        employees.add(employee);

        awsEmailSender.sendEmployeeMails(employees);

        String expected = "<table>Table 1</table>\n\n" + "<table>Table 2</table>";

        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(awsSesHandler).sendEmail(Mockito.anyString(), stringArgumentCaptor.capture(), Mockito.anyString(), Mockito.anyString());
        Assert.assertEquals(expected, stringArgumentCaptor.getValue());
    }
}

