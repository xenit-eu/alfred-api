package eu.xenit.apix.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.xenit.apix.search.SearchQuery;
import eu.xenit.apix.search.json.SearchNodeJsonParser;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Created by jasper on 04/04/17.
 */

public class SearchWebscriptTest {

    @Test
    public void Test() throws IOException {
        ObjectMapper m = new SearchNodeJsonParser().getObjectMapper();
        String jsonInput = "{\"query\":{\"property\":{\"name\":\"cm:name\",\"value\":\"Company Home\"}},\"paging\":{\"limit\":1,\"skip\":0},\"facets\":{\"enabled\":false}}";
        InputStream stream = new ByteArrayInputStream(jsonInput.getBytes(StandardCharsets.UTF_8));
        SearchQuery q = m.readValue(stream, SearchQuery.class);
        Assert.assertNotNull(q);
    }
}
