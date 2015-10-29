package eu.xenit.apix.web;

/**
 * Provides utility functions to access information about the current web context
 */
public interface IWebUtils {

    String getHost();

    int getPort();

    String getProtocol();
}
