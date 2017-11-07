import org.junit.Before;
import org.junit.Test;
import loke.HtmlTableCreator;
import loke.utils.ResourceLoaderTestUtility;

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


        String expected = ResourceLoaderTestUtility.loadResource("sql/HtmlTableTest1.html");
        String result = htmlTableCreator.createTable(head, body, null, null, true);

        assertEquals(expected, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void headIsBiggerThanBody_TrowsIllegalArgumentException() throws Exception {
        List<String> head = new ArrayList<>();
        head.add("First name");
        head.add("Last name");

        List<String> body = new ArrayList<>();
        body.add("John");


        htmlTableCreator.createTable(head, body, null, null, true);
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

        htmlTableCreator.createTable(head, body, null, null, true);
    }
}