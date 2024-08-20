package eu.xenit.alfred.api.alfresco.workflow.utils;

import org.slf4j.Logger;

public class DebugHelper {

    public static void PrintCurrentTimeElapsed(Logger logger, String message, long startTime) {
        logger.debug(message + (double) (System.nanoTime() - startTime) / 1.0E9D);
    }
}
