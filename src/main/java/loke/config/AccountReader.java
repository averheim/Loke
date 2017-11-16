package loke.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class AccountReader implements CsvReader {
    private static final Logger logger = LogManager.getLogger(AccountReader.class);

    @Override
    public Map<String, String> readCSV(String filePath) throws MalformedCSVException {
        Map<String, String> accounts = new HashMap<>();

        try (InputStream inputStream = new FileInputStream(new File(filePath));
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {

            String line = bufferedReader.readLine();
            while (line != null) {
                logger.trace("Reading line: {}", line);
                String[] split = line.split(",");
                if (split.length == 2) {
                    accounts.put(split[0], split[1]);
                    line = bufferedReader.readLine();
                } else {
                    String message = "Malformed CSV:\nExpected: key,value\nGot: " + line;
                    logger.warn(message);
                    throw new MalformedCSVException(message);
                }
            }
        } catch (IOException e) {
            logger.warn(e.getMessage());
        }
        logger.trace("Finished getting resource");
        return accounts;
    }
}
