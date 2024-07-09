package eu.xenit.apix.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class ApplicationContextSetter implements ApplicationContextAware {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationContextSetter.class);

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        if(applicationContext == null){
            logger.error("The passed applicationContext is null.");
        } else {
            logger.info("setApplicationContext with " + applicationContext.getId());
            ApplicationContextProvider.setApplicationContext(applicationContext);
        }
    }
}
