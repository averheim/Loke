package loke.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class AccountReader implements CsvReader {
    private static final Logger logger = LogManager.getLogger(AccountReader.class);

    @Override
    public Map<String, String> readCSV(String filePath) {
        logger.trace("Getting resource: {}", filePath);
        Map<String, String> accounts = new HashMap<>();
        try (InputStream inputStream = new FileInputStream(filePath);
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line = bufferedReader.readLine();
            while (line != null) {
                String[] split = line.split(",");
                accounts.put(split[0], split[1]);
                line = bufferedReader.readLine();
            }
            logger.trace("Finished getting resource");
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Failed to read resource " + filePath + ", got exception: " + e, e);
            throw new RuntimeException("Failed to read resource " + filePath + ", got exception: " + e, e);
        }
        return accounts;
    }
}
