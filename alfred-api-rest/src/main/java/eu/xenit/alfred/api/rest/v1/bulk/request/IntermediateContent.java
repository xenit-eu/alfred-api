package eu.xenit.alfred.api.rest.v1.bulk.request;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class IntermediateContent extends ServletInputStream {

    IntermediateContent(byte[] body) {
        delegate = new ByteArrayInputStream(body);
    }

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
