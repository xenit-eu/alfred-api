package eu.xenit.apix.alfresco42.search.solr;

import com.google.common.collect.Multimap;
import org.apache.commons.codec.EncoderException;

import java.io.IOException;

/**
 * Created by Stan on 15-Feb-16.
 */
public interface SolrClient {

    String post(String url, Multimap<String, String> parameters) throws IOException, EncoderException;

    String post(String url, Multimap<String, String> parameters, String body) throws IOException, EncoderException;
}
