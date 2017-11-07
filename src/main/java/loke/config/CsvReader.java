package loke.config;

import java.io.IOException;
import java.util.Map;

public interface CsvReader {
    Map<String, String> readCSV(String filePath) throws MalformedCSVException, IOException;
}
