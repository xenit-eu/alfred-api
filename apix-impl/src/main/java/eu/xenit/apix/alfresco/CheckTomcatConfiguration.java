package eu.xenit.apix.alfresco;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("eu.xenit.apix.alfresco.CheckTomcatConfiguration")
public class CheckTomcatConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(CheckTomcatConfiguration.class);
    private ApixSpringConfiguration configuration;
    private static String DEFAULT_TOMCAT_CONTEXT_PATH = "/usr/local/tomcat/webapps/alfresco/META-INF/context.xml";

    @Autowired
    public CheckTomcatConfiguration(ApixSpringConfiguration configuration) {
        this.configuration = configuration;
        readTomcatContextFile();
    }


    public void readTomcatContextFile() {
        // TODO: tomcat-embedded rmi/JNDI is broken
        // Set RMI ON with tomcat embedded, but no JNDI is possible...
        // Context ctx = new InitialContext();
        // String value = (String) ctx.lookup("java:comp/env/myConfig");
        try {
            logger.debug("Global alfresco properties: {}", configuration.getProperties());
            String stringPathTomcatContext = configuration.getProperties()
                    .getProperty("tomcat.context.path", DEFAULT_TOMCAT_CONTEXT_PATH);
            Path pathTomcatContext = Path.of(stringPathTomcatContext);
            logger.info("pathTomcatContext {}", pathTomcatContext);

            // Path not set and warning user to make sure the context is set.
            if (configuration.getProperties().getProperty("tomcat.context.path") == null) {
                logger.warn(
                        "Global property - tomcat.context.path is not set. Make sure you have defined the servletContext of Tomcat correctly to use Alfred-API.\n "
                                + "If you have issues with RequestPart || forward-Slashes your Tomcat context is not set correctly.");
            } else {
                // Check fysical context.xml file for configuration
                String tomcatContext = Files.readString(pathTomcatContext, StandardCharsets.UTF_8);
                logger.debug("context.xml {}", tomcatContext);

                if (tomcatContext == null) {
                    logger.error(
                            "No Tomcat context.xml is found under Path: " + pathTomcatContext
                                    + ".\n");
                    return;
                }
                if (!(tomcatContext.contains("allowMultipleLeadingForwardSlashInPath") && tomcatContext.contains(
                        "allowCasualMultipartParsing"))) {
                    logger.warn("The Tomcat context.xml was found but is missing crucial setup!\n "
                            + "See if allowCasualMultipartParsing=\"true\" allowMultipleLeadingForwardSlashInPath=\"true\" are set in the <Context ... > block.\n"
                            + "Can be ignored if the tomcat_context is set via other methods than the context.xml.");
                    return;
                }
            }
        } catch (Exception e) {
            logger.error("Failed to read context.xml", e);
        }
    }
}
