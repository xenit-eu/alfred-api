package eu.xenit.apix.server;

import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component("eu.xenit.apix.server.ApplicationContextSetter")
public class ApplicationContextSetter implements ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationContextSetter.class);

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        if (applicationContext == null) {
            throw new NullPointerException();
        } else {
            logger.info("setApplicationContext with {}" + applicationContext.getId());
            Server.setApplicationContext(applicationContext);
        }
    }
}
