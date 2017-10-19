import org.junit.Before;
import org.junit.Test;
import utils.HtmlTableCreator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class HtmlTableCreatorTest {
    private HtmlTableCreator htmlTableCreator;

    @Before
    public void setUp() throws Exception {
        htmlTableCreator = new HtmlTableCreator();
    }

    @Test
    public void onlyHeadAndBody_returnsNoFoot() throws Exception {
        List<String> head = new ArrayList<>();
        head.add("First name");
        head.add("Last name");

        List<String> body = new ArrayList<>();
        body.add("John");
        body.add("Doe");
        body.add("Jane");
        body.add("Doe");

        String expected = loadResource("HtmlTableTest1.html");
        String result = htmlTableCreator.createTable(head, body, null);

        assertEquals(expected, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void headIsBiggerThanBody_TrowsIllegalArgumentException() throws Exception {
        List<String> head = new ArrayList<>();
        head.add("First name");
        head.add("Last name");

        List<String> body = new ArrayList<>();
        body.add("John");

        htmlTableCreator.createTable(head, body, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void bodyAndHeadIsNotDivisible_ThrowsIllegalArgumentException() throws Exception {
        List<String> head = new ArrayList<>();
        head.add("First name");
        head.add("Last name");

        List<String> body = new ArrayList<>();
        body.add("John");
        body.add("Doe");
        body.add("Jane");

        htmlTableCreator.createTable(head, body, null);
    }

    private String loadResource(String resourceName) throws IOException {
        InputStream inputStream = ClassLoader.getSystemResourceAsStream(resourceName);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String line = bufferedReader.readLine();
        while (line != null) {
            stringBuilder.append(line.trim());
            line = bufferedReader.readLine();
        }
        return stringBuilder.toString();
    }
}