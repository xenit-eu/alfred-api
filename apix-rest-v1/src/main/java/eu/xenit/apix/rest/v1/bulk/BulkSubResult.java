package eu.xenit.apix.rest.v1.bulk;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.ApiModelProperty;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by kenneth on 06.06.16.
 */
public class BulkSubResult {

    @ApiModelProperty(value = "The HTTP status code of the sub call", example = "200")
    private int statusCode;
    @ApiModelProperty(value = "JSON result body of the sub call", dataType = "object")
    private JsonNode body;
    private Map<String, String> headers;

    public BulkSubResult() {

    }

    public BulkSubResult(int statusCode, JsonNode body, Map<String, String> headers) {
        this.statusCode = statusCode;
        this.body = body;
        this.headers = headers;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public JsonNode getBody() {
        return body;
    }

    public void setBody(JsonNode body) {
        this.body = body;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(HashMap<String, String> headers) {
        this.headers = headers;
    }
}
