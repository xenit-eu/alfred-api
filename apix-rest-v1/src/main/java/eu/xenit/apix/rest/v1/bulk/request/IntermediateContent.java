package eu.xenit.apix.rest.v1.bulk.request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class IntermediateContent extends ServletInputStream {
    IntermediateContent(Object body) throws JsonProcessingException {
        mapper = new ObjectMapper();
        delegate = new ByteArrayInputStream(mapper.writeValueAsBytes(body));
    }

    final ObjectMapper mapper;
    final InputStream delegate;

    @Override
    public int read() throws IOException {
        return delegate.read();
    }

    @Override
    public boolean isFinished() {
        return false;
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void setReadListener(ReadListener readListener) {
        //will not be  implemented
    }

}
