package eu.xenit.apix.alfresco42.search.solr;

import com.google.common.collect.Multimap;
import org.alfresco.httpclient.HttpClientFactory;
import org.alfresco.repo.search.impl.lucene.LuceneQueryParserException;
import org.alfresco.repo.search.impl.solr.SolrChildApplicationContextFactory;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Created by Stan on 15-Feb-16.
 */
@Component
public class SolrClientImpl implements SolrClient {

    private final Logger logger = LoggerFactory.getLogger(SolrClientImpl.class);

    @Autowired()
    @Resource(name = "solr")
    SolrChildApplicationContextFactory solrhttpClientFactory;

    @Override
    public String post(String url, Multimap<String, String> parameters) throws IOException, EncoderException {
        return this.post(url, parameters, null);
    }

    @Override
    public String post(String url, Multimap<String, String> parameters, String body)
            throws IOException, EncoderException {
        HttpClientFactory httpClientFactory = (HttpClientFactory) (solrhttpClientFactory).getApplicationContext()
                .getBean("solrHttpClientFactory");
        final HttpClient httpClient = httpClientFactory.getHttpClient();
        HttpClientParams params = httpClient.getParams();
        params.setBooleanParameter(HttpClientParams.PREEMPTIVE_AUTHENTICATION, true);

        final URLCodec encoder = new URLCodec();
        StringBuilder urlBuilder = new StringBuilder();
        for (Map.Entry<String, String> entry : parameters.entries()) {
            if (urlBuilder.length() == 0) {
                urlBuilder.append("?");
            } else {
                urlBuilder.append("&");
            }
            String value = entry.getValue();
            if (value.indexOf('+') == -1) {
                value = encoder.encode(value, "UTF-8");
            }
            urlBuilder.append(encoder.encode(entry.getKey())).append("=").append(value);
        }
        urlBuilder.insert(0, url);
        logger.debug("parameters {}", parameters);

        final String uri = urlBuilder.toString();

        logger.debug("solr query: {}", uri);

        PostMethod post = new PostMethod(uri);
        if (body != null) {
            post.setRequestEntity(
                    new ByteArrayRequestEntity(body.getBytes(StandardCharsets.UTF_8), "application/json"));
        }

        try {
            long startTime = 0;
            if (logger.isDebugEnabled()) {
                startTime = System.currentTimeMillis();
            }
            httpClient.executeMethod(post);
            if (logger.isDebugEnabled()) {
                long endTime = System.currentTimeMillis();
                logger.debug("TIMING - solr took " + (endTime - startTime) + "ms to respond.");
            }

            if (post.getStatusCode() == HttpStatus.SC_MOVED_PERMANENTLY
                    || post.getStatusCode() == HttpStatus.SC_MOVED_TEMPORARILY) {
                Header locationHeader = post.getResponseHeader("location");
                if (locationHeader != null) {
                    String redirectLocation = locationHeader.getValue();
                    post.setURI(new URI(redirectLocation, true));
                    httpClient.executeMethod(post);
                }
            }

            if (post.getStatusCode() != HttpServletResponse.SC_OK) {
                logger.error("HTTP error: " + post.getResponseBodyAsString());
                throw new LuceneQueryParserException("Request failed " + post.getStatusCode());
            }

            return post.getResponseBodyAsString();

        } finally {
            post.releaseConnection();
        }
    }

}
