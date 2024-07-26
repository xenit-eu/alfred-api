package eu.xenit.apix.rest;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

public class CheckTomcatConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(CheckTomcatConfiguration.class);

    public CheckTomcatConfiguration() {
        readTomcatContextFile();
    }

    public void readTomcatContextFile(){
        try {
        // TODO: tomcat-embedded rmi/JNDI is broken
        // Set RMI ON with tomcat embedded, but no JNDI is possible...
//            Context ctx = new InitialContext();
//            String value = (String) ctx.lookup("java:comp/env/myConfig");
//            logger.error("JNDI ctx.lookup(\"java:comp/env/env/myConfig\"): ", value);

            Resource resource = new FileSystemResource("/usr/local/tomcat/webapps/alfresco/META-INF/context.xml");
            String reader = new BufferedReader(
                    new InputStreamReader(
                            resource.getInputStream(), StandardCharsets.UTF_8
                    )
            ).lines().collect(Collectors.joining());
            final String tomcatContext = reader;
            if(reader == null) {
                logger.error("No Tomcat context.xml is found under Path: \"/usr/local/tomcat/webapps/alfresco/META-INF/context.xml\".\n "
                        + "Check manually if the context.xml contains allowMultipleLeadingForwardSlashInPath AND allowCasualMultipartParsing = true.");
                return;
            }
            logger.info("context.xml ", tomcatContext);

            if( ! (tomcatContext.contains("allowMultipleLeadingForwardSlashInPath") && tomcatContext.contains("allowCasualMultipartParsing")) ) {
                logger.error("The Tomcat context.xml was found but is missing crucial setup!");
                printWarning();
                return;
            }
            logger.info("The Tomcat context.xml was found and has the correct setup for Apix.");

        }catch (Exception e) {
            logger.error("Failed to read META-INF/context.xml",e );
            printWarning();
        }
    }

    protected void printWarning(){
        logger.error("There is a chance your Tomcat /META-INF/context.xml file is auto-generated and does not contain the necessary configuration.\n"
                + "Please log into your alfresco container and go to ${TOMCAT_HOME}/webapps/alfresco/META-INF/context.xml\n "
                + "See if allowCasualMultipartParsing=\"true\" allowMultipleLeadingForwardSlashInPath=\"true\" are set in the <Context ... > block.\n"
                + "To resolve this error, set the context.xml and web.xml with name=jdbc/myTomcat to initialise the JNDI + Context configuration.\n");
    }
}
