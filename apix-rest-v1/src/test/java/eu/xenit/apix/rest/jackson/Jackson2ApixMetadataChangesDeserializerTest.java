package eu.xenit.apix.rest.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.xenit.apix.data.QName;
import eu.xenit.apix.node.MetadataChanges;
import eu.xenit.apix.rest.AlfredApiRestServletContext;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class Jackson2ApixMetadataChangesDeserializerTest {
    private static final String input = "{" +
            "\"type\":\"cm:content\","+
            "\"aspectsToAdd\":["+
            "\"{http://www.alfresco.org/model/content/1.0}auditable\""+
            "],"+
            "\"aspectsToRemove\":[],"+
            "\"propertiesToSet\":{"+
            "\"{http://www.alfresco.org/model/content/1.0}name\":[\"test.pdf\"],"+
            "\"{http://www.alfresco.org/model/content/1.0}title\":[\"test.pdf\"]"+
            "}"+
        "}";

    @Test
    public void serialize() throws JsonProcessingException {
        MetadataChanges output = new MetadataChanges();
        output.setType(new QName("cm:content"));
        QName[] aspects = new QName[1];
        aspects[0] = new QName("{http://www.alfresco.org/model/content/1.0}auditable");
        output.setAspectsToAdd(aspects);
        output.setAspectsToRemove(new QName[0]);
        Map<QName, String[]> metadata = new HashMap<>();
        String[] values = new String[1];
        values[0] = "test.pdf";
        metadata.put(new QName("{http://www.alfresco.org/model/content/1.0}name"), values);
        metadata.put(new QName("{http://www.alfresco.org/model/content/1.0}title"), values);
        output.setPropertiesToSet(metadata);

        AlfredApiRestServletContext config =
                new AlfredApiRestServletContext(null, null);
        assertNotNull(config);
        assertNotNull(config.objectMapper());
        assertNotNull(config.objectMapper().writeValueAsString(output));
        MetadataChanges sample = config.objectMapper().readValue(input, MetadataChanges.class);
        assertEquals(sample, output);
        assertEquals(input, config.objectMapper().writeValueAsString(output));
    }

    @Test
    public void deserialize() throws JsonProcessingException {
        AlfredApiRestServletContext config =
                new AlfredApiRestServletContext(null, null);
        assertNotNull(config);
        assertNotNull(config.objectMapper());
        MetadataChanges c = config.objectMapper().readValue(input, MetadataChanges.class);
        assertNotNull(c);
        assertEquals("cm:content", c.getType().getValue());
        assertEquals(1, c.getAspectsToAdd().length);
        assertEquals("{http://www.alfresco.org/model/content/1.0}auditable", c.getAspectsToAdd()[0].getValue());
        assertEquals(0, c.getAspectsToRemove().length);
        assertEquals(2, c.getPropertiesToSet().size());
        assertTrue("should contain",
                c.getPropertiesToSet().containsKey(new QName("{http://www.alfresco.org/model/content/1.0}title")));
        assertTrue("should contain",
                c.getPropertiesToSet().containsKey(new QName("{http://www.alfresco.org/model/content/1.0}name")));
    }

    @Test
    public void testDeserializer() {
        AlfredApiRestServletContext config =
                new AlfredApiRestServletContext(null, null);
        assertNotNull(config);
        assertNotNull(config.objectMapper());
        Jackson2ApixMetadataChangesDeserializer deserializer
                = new Jackson2ApixMetadataChangesDeserializer(config.objectMapper());
        MetadataChanges c = deserializer.convert(input);
        assertNotNull(c);
        assertEquals("cm:content", c.getType().getValue());
        assertEquals(1, c.getAspectsToAdd().length);
        assertEquals("{http://www.alfresco.org/model/content/1.0}auditable", c.getAspectsToAdd()[0].getValue());
        assertEquals(0, c.getAspectsToRemove().length);
        assertEquals(2, c.getPropertiesToSet().size());
        assertTrue("should contain",
                c.getPropertiesToSet().containsKey(new QName("{http://www.alfresco.org/model/content/1.0}title")));
        assertTrue("should contain",
                c.getPropertiesToSet().containsKey(new QName("{http://www.alfresco.org/model/content/1.0}name")));
    }
}
