package eu.xenit.apix.server;

import org.springframework.context.ApplicationContext;

public class ApplicationContextProvider {
    private static ApplicationContext applicationContext;

    public static void setApplicationContext(ApplicationContext context) {
        applicationContext = context;
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }
}
