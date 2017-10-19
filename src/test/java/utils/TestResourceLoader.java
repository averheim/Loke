package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class TestResourceLoader {
    public static String loadResource(String resourceName) throws IOException {
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
