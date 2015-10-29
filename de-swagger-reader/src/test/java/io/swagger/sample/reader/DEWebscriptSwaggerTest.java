package io.swagger.sample.reader;


import com.fasterxml.jackson.core.JsonProcessingException;
import eu.xenit.swagger.reader.Reader;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.PathParameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.util.Json;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Created by Michiel Huygen on 02/03/2016.
 */
public class DEWebscriptSwaggerTest {

    private final static Logger logger = LoggerFactory.getLogger(DEWebscriptSwaggerTest.class);

    private Reader reader;

    @Before
    public void Setup() {
        reader = new Reader(new Swagger());
        reader.read(DEWebscript.class);
    }

    @After
    public void TearDown() throws JsonProcessingException {
        String json = Json.mapper().writeValueAsString(reader.getSwagger());
        logger.debug(json);
    }

    @Test
    public void TestParser() throws IOException {

        String json = Json.mapper().writeValueAsString(reader.getSwagger());
        logger.debug(json);
    }

    @Test
    public void TestDetectWebscriptAnnotation() throws JsonProcessingException {
        assertFalse(reader.getSwagger().getPaths().isEmpty());
    }

    @Test
    public void TestReadUriAnnotation() throws JsonProcessingException {
        Path path = reader.getSwagger().getPath("/de/home"); // Testing basepath
        assertNotNull(path);

        Operation get = path.getGet();

        assertNotNull(get);

        assertEquals("core", get.getTags().get(0));
        assertEquals("/home summary", get.getSummary());
        assertEquals("/home description", get.getDescription());

        logger.debug(path.toString());
    }

    @Test
    public void TestReadParameters() throws JsonProcessingException {

        Path path = reader.getSwagger().getPath("/de/home");
        Operation get = path.getGet();
        logger.debug(get.getParameters().toString());

        QueryParameter p1 = (QueryParameter) get.getParameters().get(0);
        QueryParameter p2 = (QueryParameter) get.getParameters().get(1);
        PathParameter p3 = (PathParameter) get.getParameters().get(2);
        BodyParameter p4 = (BodyParameter) get.getParameters().get(3);
        assertEquals("requestParam", p1.getName());
        assertEquals("query", p1.getIn());
        assertEquals(true, p1.getRequired());
        assertEquals("string", p1.getType());

        assertEquals("requiredParam", p2.getName());
        assertEquals(false, p2.getRequired());
        assertEquals("string", p1.getType());

        assertEquals("pathParam", p3.getName());
        assertEquals("path", p3.getIn());
        assertEquals("string", p1.getType());

        assertEquals("body", p4.getName()); // Is this correct
        assertEquals("body", p4.getIn());
        //assertEquals("body", p4.getSchema());

    }

    @Test
    public void TestIgnoreWebscriptRequestAndResponse() throws JsonProcessingException {

        Path path = reader.getSwagger().getPath("/de/ignoreParams");
        Operation get = path.getGet();
        assertEquals(0, get.getParameters().size());
        logger.debug(get.getParameters().toString());
    }

}
