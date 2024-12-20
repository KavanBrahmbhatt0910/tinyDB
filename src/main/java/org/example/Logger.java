package org.example;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The Logger class provides logging functionality for different categories.
 */
public class Logger {
    private static final String LOG_DIRECTORY_GENERAL = "logs/general/";
    private static final String LOG_DIRECTORY_QUERY = "logs/query/";
    private static final String LOG_DIRECTORY_EVENT = "logs/event/";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final String LOG_FILE = "_tinydb.log";
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * Logs a message with the specified log category.
     *
     * @param category The log category.
     * @param message  The message to log.
     */
    private static void log(String directoryPath, LogCategory category, String message) {
        String today = DATE_FORMAT.format(new Date());

        try (PrintWriter out = new PrintWriter(new FileWriter(directoryPath + today  + LOG_FILE, true))) {
            String timestamp = sdf.format(new Date());
            out.printf("[%s] --- [%s] %s%n", timestamp, category, message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Logs a message with the GENERAL log category.
     *
     * @param message The message to log.
     */
    public static void logGeneral(String message) {
        // Create the logs directory if it doesn't exist
        File generalLogDir = new File(LOG_DIRECTORY_GENERAL);

        if (!generalLogDir.exists()) {
            if (generalLogDir.mkdirs()) {
//                System.out.println("General logs directory created successfully.");
            } else {
//                System.err.println("Failed to create logs directory!");
            }
        }

        log(LOG_DIRECTORY_GENERAL, LogCategory.GENERAL, message);
    }

    /**
     * Logs a message with the QUERY log category.
     *
     * @param message The message to log.
     */
    public static void logQuery(String message) {
        // Create the logs directory if it doesn't exist
        File queryLogDir = new File(LOG_DIRECTORY_QUERY);

        if (!queryLogDir.exists()) {
            if (queryLogDir.mkdirs()) {
//                System.out.println("General logs directory created successfully.");
            } else {
//                System.err.println("Failed to create logs directory!");
            }
        }

        log(LOG_DIRECTORY_QUERY, LogCategory.QUERY, message);
    }

    /**
     * Logs a message with the EVENT log category.
     *
     * @param message The message to log.
     */
    public static void logEvent(String message) {
        // Create the logs directory if it doesn't exist
        File eventLogDir = new File(LOG_DIRECTORY_EVENT);

        if (!eventLogDir.exists()) {
            if (eventLogDir.mkdirs()) {
//                System.out.println("General logs directory created successfully.");
            } else {
//                System.err.println("Failed to create logs directory!");
            }
        }

        log(LOG_DIRECTORY_EVENT, LogCategory.EVENT, message);
    }

    /**
     * Enum representing different log categories.
     */
    public enum LogCategory {
        GENERAL, QUERY, EVENT
    }
}
