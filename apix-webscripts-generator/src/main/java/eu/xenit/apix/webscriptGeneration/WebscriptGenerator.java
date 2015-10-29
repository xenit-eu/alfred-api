package eu.xenit.apix.webscriptGeneration;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import java.util.HashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Automatically generates valid alfresco webscripts
 * <p/>
 * Takes dynamic extensions webscripts as input and outputs all files required to host the same webscripts in default
 * alfresco webscripts Created by Michiel Huygen on 29/03/2016.
 */
public class WebscriptGenerator {

    private final Template webscriptDescXmlTemplate;
    private final Template webscriptJavaTemplate;
    private final Template springConfigurationXmlTemplate;
    private Logger logger = LoggerFactory.getLogger(WebscriptGenerator.class);

    public WebscriptGenerator() throws IOException {
        try {
            springConfigurationXmlTemplate = compileTemplate("SpringConfiguration.xml.mustache");
            webscriptDescXmlTemplate = compileTemplate("Webscript.desc.xml.mustache");
            webscriptJavaTemplate = compileTemplate("Webscript.java.mustache");
        } catch (IOException e) {
            logger.error("Can't load mustache templates for webscript generation", e);
            throw e;
        }
    }

    private Template compileTemplate(String classpathResourceTemplateFile) throws IOException {
        InputStream s = null;
        try {

            s = this.getClass().getResourceAsStream(classpathResourceTemplateFile);
            return Mustache.compiler().compile(new InputStreamReader(s));

        } finally {
            if (s != null) {
                s.close();
            }
        }
    }


    public String generateDescXmlFilename(WebscriptDefinition def) {
        return def.getPackage().replaceAll("\\.", "/") + "/" + def.getId() + "." + def.getMethod() + ".desc.xml";
    }

    public String generateDescXmlContent(WebscriptDefinition def) {
        return webscriptDescXmlTemplate.execute(def);
    }

    public String generateSpringXml(final List<WebscriptDefinition> webscriptDefinitions) {
        validate(webscriptDefinitions);
        HashMap<Object, Object> model = new HashMap<>();
        model.put("webscripts", webscriptDefinitions);
        return springConfigurationXmlTemplate.execute(model);
    }

    private void validate(List<WebscriptDefinition> webscriptDefinitions) {
        for (WebscriptDefinition def : webscriptDefinitions) {
            validate(def);
        }
    }

    private void validate(WebscriptDefinition def) {
        if (def.getPackage() == null || def.getPackage().isEmpty()) {
            throw new UnsupportedOperationException("Default or empty package is not allowed!" + def);
        }
    }

    public String generateJavaClass(WebscriptDefinition def) {
        return webscriptJavaTemplate.execute(def);
    }

    public String generateJavaClassFilename(WebscriptDefinition def) {
        return def.getPackage().replaceAll("\\.", "/") + "/" + def.getId() + def.getMethod().toUpperCase() + ".java";
    }
}
