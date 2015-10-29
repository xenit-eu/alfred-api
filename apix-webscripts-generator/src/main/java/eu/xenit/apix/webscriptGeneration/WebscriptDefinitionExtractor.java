package eu.xenit.apix.webscriptGeneration;

import com.github.dynamicextensionsalfresco.webscripts.annotations.Authentication;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Extracts WebscriptDefinitions from DE webscript annotations
 * <p/>
 * Created by Michiel Huygen on 29/03/2016.
 */
public class WebscriptDefinitionExtractor {

    public List<WebscriptDefinition> extractWebscripts(Class cls) {
        WebScript webscript = (WebScript) cls.getAnnotation(WebScript.class);
        if (webscript == null) {
            return Arrays.asList();
        }

        ArrayList<WebscriptDefinition> ret = new ArrayList<>();

        if (webscript.families().length > 2) {
            throw new UnsupportedOperationException("Multiple families are not supported" + webscript);
        }
        String family = "";

        if (webscript.families().length == 1) {
            family = webscript.families()[0];
        }

        String baseAuth = "user";
        Authentication baseAuthAnn = (Authentication) cls.getAnnotation(Authentication.class);
        if (baseAuthAnn != null) {
            validateAuthenticationAnnotation(baseAuthAnn);
            baseAuth = baseAuthAnn.value().toString().toLowerCase();
        }

        for (java.lang.reflect.Method m : cls.getMethods()) {
            Uri uri = m.getAnnotation(Uri.class);
            if (uri == null) {
                continue;
            }

            WebscriptDefinition def = new WebscriptDefinition();

            if (uri.value().length == 0) {
                throw new UnsupportedOperationException("@Uri without uri is not supported!");
            }

            if (uri.value().length != 1) {
                throw new UnsupportedOperationException("Only @Uri with a single url is supported!");
            }

            def.setUrl(webscript.baseUri() + uri.value()[0]);
            def.setId(cls.getSimpleName() + "_" + m.getName());
            def.setShortName(def.getId());
            def.setPackage(cls.getPackage().getName());
            def.setFamily(family);
            def.setMethod(uri.method().toString().toLowerCase());
            def.setAuthentication(baseAuth);
            def.setDescription("Description of " + def.getId());
            def.setClazz(cls);
            def.setTargetMethod(m);

            Authentication authAnn = m.getAnnotation(Authentication.class);
            if (authAnn != null) {
                validateAuthenticationAnnotation(authAnn);
                def.setAuthentication(authAnn.value().toString().toLowerCase());
            }

            ret.add(def);

        }

        return ret;
    }

    private void validateAuthenticationAnnotation(Authentication baseAuthAnn) {
        if (!baseAuthAnn.runAs().isEmpty()) {
            throw new UnsupportedOperationException("runAs user on @Authentication is not supported");
        }
    }

    public ArrayList<WebscriptDefinition> extractWebscripts(List<Class<?>> classes) {
        ArrayList<WebscriptDefinition> ret = new ArrayList<WebscriptDefinition>();
        for (Class cls : classes) {
            ret.addAll(extractWebscripts(cls));
        }
        return ret;
    }
}
