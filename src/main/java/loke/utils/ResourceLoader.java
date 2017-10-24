package loke.utils;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Reads a file resource in classpath to a string
 */
public class ResourceLoader {
    private static final Logger logger = LogManager.getLogger();

    public static String getResource(String theResourceName) {
        logger.trace("Getting resource: {}", theResourceName);
        try {
            InputStream anIn = ResourceLoader.class.getClassLoader().getResourceAsStream(theResourceName);
            BufferedReader aBufferedReader = new BufferedReader(new InputStreamReader(anIn));
            StringBuilder aResource = new StringBuilder();
            String aLine = aBufferedReader.readLine();
            while (aLine != null) {
                aResource.append(aLine);
                aResource.append("\n");
                aLine = aBufferedReader.readLine();
            }
            aBufferedReader.close();
            logger.trace("Finished getting resource");
            return aResource.toString();
        } catch (Exception e) {
            logger.error("Failed to read resource " + theResourceName + ", got exception: " + e, e);
            throw new RuntimeException("Failed to read resource " + theResourceName + ", got exception: " + e, e);
        }
    }
}
