package eu.xenit.apix.webscriptGeneration;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

/**
 * Created by Michiel Huygen on 29/03/2016.
 */
public class JMustacheTest {

    @Test
    public void TestTemplateEngine() {
        String text = "One, two, {{three}}. Three sir!";
        Template tmpl = Mustache.compiler().compile(text);
        Map<String, String> data = new HashMap<String, String>();
        data.put("three", "five");
        System.out.println(tmpl.execute(data));
        // result: "One, two, five. Three sir!"
    }
}
