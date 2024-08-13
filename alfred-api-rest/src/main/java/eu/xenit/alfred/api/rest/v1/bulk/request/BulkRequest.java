package eu.xenit.alfred.api.rest.v1.bulk.request;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Represents a single request in a bulk requests call Created by kenneth on 18.03.16.
 */
public class BulkRequest {

    private String method;
    private String url;
    private JsonNode body;

    public BulkRequest() {
    }

    public BulkRequest(String method, String url, JsonNode body) {
        this.method = method;
        this.url = url;
        this.body = body;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public JsonNode getBody() {
        return body;
    }

    public void setBody(JsonNode body) {
        this.body = body;
    }
}
