package eu.xenit.apix.rest.v1.bulk;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import org.apache.commons.io.IOUtils;
import org.springframework.extensions.surf.util.Content;

public class IntermediateContent implements Content {

    // According to /etc/mime.types, this is the only one
    private final String mimetype = "application/json";
    private String json;
    // Should always be UTF-8 but since our json comes from the parent Content,
    // we'll get the encoding from there too
    private String encoding;

    public IntermediateContent(Content parent, JsonNode content) {
        json = (content != null) ? content.toString() : "{}";
        encoding = parent.getEncoding();
    }

    public String getContent() {
        return json;
    }

    @Override
    public String getMimetype() {
        return mimetype;
    }

    @Override
    public String getEncoding() {
        return encoding;
    }

    @Override
    public long getSize() {
        return json.length();
    }

    @Override
    public InputStream getInputStream() {
        return IOUtils.toInputStream(json);
    }

    @Override
    public Reader getReader() throws IOException {
        return new StringReader(json);
    }
}
