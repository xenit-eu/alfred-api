package eu.xenit.apix.tests.helperClasses.alfresco.services;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.alfresco.repo.content.encoding.ContentCharsetFinder;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.apache.commons.io.IOUtils;

public class AlfrescoMimetypeServiceStub implements MimetypeService {

    public String hardMimeType;

    public String getHardMimeType() {
        return hardMimeType;
    }

    public void setHardMimeType(String hardMimeType) {
        this.hardMimeType = hardMimeType;
    }

    @Override
    public String getExtension(String mimetype) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getMimetype(String extension) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, String> getDisplaysByExtension() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, String> getDisplaysByMimetype() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, String> getExtensionsByMimetype() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, String> getMimetypesByExtension() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isText(String mimetype) {
        return false;
    }

    @Override
    public List<String> getMimetypes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String guessMimetype(String filename) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String guessMimetype(String filename, ContentReader reader) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String guessMimetype(String filename, InputStream input) {
        try {
            return hardMimeType;
        } finally {
            IOUtils.closeQuietly(input);
        }
    }

    @Override
    public String getMimetypeIfNotMatches(ContentReader reader) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ContentCharsetFinder getContentCharsetFinder() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<String> getMimetypes(String extension) {
        throw new UnsupportedOperationException();
    }
}
