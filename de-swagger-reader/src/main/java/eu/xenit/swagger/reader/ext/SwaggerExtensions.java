package eu.xenit.swagger.reader.ext;

import eu.xenit.swagger.reader.DESwaggerExtension;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SwaggerExtensions {

    static Logger LOGGER = LoggerFactory.getLogger(SwaggerExtensions.class);

    private static List<SwaggerExtension> extensions = null;

    static {
        extensions = new ArrayList<SwaggerExtension>();
        ServiceLoader<SwaggerExtension> loader = ServiceLoader.load(SwaggerExtension.class);
        Iterator<SwaggerExtension> itr = loader.iterator();
        while (itr.hasNext()) {
            SwaggerExtension ext = itr.next();
            LOGGER.debug("adding extension " + ext);
            extensions.add(ext);
        }
        extensions.add(new DESwaggerExtension());
    }

    public static List<SwaggerExtension> getExtensions() {
        return extensions;
    }

    public static void setExtensions(List<SwaggerExtension> ext) {
        extensions = ext;
    }

    public static Iterator<SwaggerExtension> chain() {
        return extensions.iterator();
    }
}