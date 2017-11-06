package loke.config;

import java.util.Map;

public interface CsvReader {
    Map<String, String> readCSV(String filePath);
}
