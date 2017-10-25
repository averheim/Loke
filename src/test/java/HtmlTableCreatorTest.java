import org.junit.Before;
import org.junit.Test;
import loke.HtmlTableCreator;
import loke.utils.TestResourceLoader;

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
        List<List<String>> bodys = new ArrayList<>();
        head.add("First name");
        head.add("Last name");

        List<String> body = new ArrayList<>();
        body.add("John");
        body.add("Doe");
        body.add("Jane");
        body.add("Doe");

        bodys.add(body);

        String expected = TestResourceLoader.loadResource("HtmlTableTest1.html");
        String result = htmlTableCreator.createTable(head, bodys, null, null);

        assertEquals(expected, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void headIsBiggerThanBody_TrowsIllegalArgumentException() throws Exception {
        List<String> head = new ArrayList<>();
        List<List<String>> bodys = new ArrayList<>();
        head.add("First name");
        head.add("Last name");

        List<String> body = new ArrayList<>();
        body.add("John");

        bodys.add(body);

        htmlTableCreator.createTable(head, bodys, null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void bodyAndHeadIsNotDivisible_ThrowsIllegalArgumentException() throws Exception {
        List<String> head = new ArrayList<>();
        List<List<String>> bodys = new ArrayList<>();
        head.add("First name");
        head.add("Last name");

        List<String> body = new ArrayList<>();
        body.add("John");
        body.add("Doe");
        body.add("Jane");

        bodys.add(body);

        htmlTableCreator.createTable(head, bodys, null, null);
    }
}