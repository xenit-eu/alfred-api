package eu.xenit.apix.rest.v1.tests.temp;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.xenit.apix.rest.v1.tests.RestV1BaseTest;
import eu.xenit.apix.search.SearchQueryResult;
import java.io.IOException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by stan on 5/3/16.
 */
public class V1SearchWebscriptTest extends RestV1BaseTest {

    private final static Logger logger = LoggerFactory.getLogger(V1SearchWebscriptTest.class);

    @Test
    public void testSearch() throws IOException {
        String checkoutUrl = makeAlfrescoBaseurl("admin", "admin") + "/apix/v1/search";
        final CloseableHttpClient checkoutHttpclient = HttpClients.createDefault();
        final HttpPost checkoutHttppost = new HttpPost(checkoutUrl);
        String checkoutJsonString = json(
                "{ \n" +
                        "  'paging': { 'limit': 15 }, \n" +
                        "  'query':  { 'type': 'cm:content' }, \n" +
                        "  'facets': { 'enabled': true } \n" +
                        "}");

        checkoutHttppost.setEntity(new StringEntity(checkoutJsonString, ContentType.APPLICATION_JSON));

        CloseableHttpResponse response = checkoutHttpclient.execute(checkoutHttppost);
        if (500 == response.getStatusLine().getStatusCode()) {
            //Internal server error!
            logger.error(response.getStatusLine().getReasonPhrase());
            logger.error(response.toString());
        }
        Assert.assertEquals(200, response.getStatusLine().getStatusCode());

        ObjectMapper mapper = new ObjectMapper();
        SearchQueryResult result = mapper
                .readValue(EntityUtils.toString(response.getEntity()), SearchQueryResult.class);

        logger.debug(String.valueOf(result));
        logger.debug(String.valueOf(result.getTotalResultCount()));
        Assert.assertFalse(result.getFacets().isEmpty());
        Assert.assertFalse(result.getNoderefs().isEmpty());
        Assert.assertFalse(result.getTotalResultCount() == 0L);
    }

}
