package eu.xenit.apix.rest.v1.bulk;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.ApiModelProperty;

/**
 * Represents a single request in a bulk requests call Created by kenneth on 18.03.16.
 */
public class BulkRequest {

    @ApiModelProperty(required = true, allowableValues = "get,put,post,delete")
    private String method;
    @ApiModelProperty(example = "/version?alf_ticket=TICKET_4654...", required = true)
    private String url;
    @ApiModelProperty(dataType = "object", notes = "Only allowed for PUT and POST")
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
