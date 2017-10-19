import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class HtmlTableCreatorTest {

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
        String result = new HtmlTableCreator().createTable(head, body, null);

        assertEquals(expected, result);
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