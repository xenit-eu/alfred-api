package eu.xenit.apix.tests.helperClasses.alfresco.services;

import eu.xenit.apix.tests.helperClasses.alfresco.entities.ContentWriterStub;
import java.util.List;
import java.util.Map;
import org.alfresco.repo.content.transform.ContentTransformer;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NoTransformerException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.service.namespace.QName;

public class AlfrescoContentServiceStub implements ContentService {

    @Override
    public long getStoreTotalSpace() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getStoreFreeSpace() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ContentReader getRawReader(String contentUrl) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ContentReader getReader(NodeRef nodeRef, QName propertyQName)
            throws InvalidNodeRefException, InvalidTypeException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ContentWriter getWriter(NodeRef nodeRef, QName propertyQName, boolean update)
            throws InvalidNodeRefException, InvalidTypeException {
        return new ContentWriterStub();
    }

    @Override
    public ContentWriter getTempWriter() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void transform(ContentReader reader, ContentWriter writer)
            throws NoTransformerException, ContentIOException {

    }

    @Override
    public void transform(ContentReader reader, ContentWriter writer, Map<String, Object> options)
            throws NoTransformerException, ContentIOException {

    }

    @Override
    public void transform(ContentReader reader, ContentWriter writer, TransformationOptions options)
            throws NoTransformerException, ContentIOException {

    }

    @Override
    public ContentTransformer getTransformer(String sourceMimetype, String targetMimetype) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ContentTransformer> getTransformers(String sourceUrl, String sourceMimetype, long sourceSize,
            String targetMimetype, TransformationOptions options) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ContentTransformer getTransformer(String sourceUrl, String sourceMimetype, long sourceSize,
            String targetMimetype, TransformationOptions options) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ContentTransformer getTransformer(String sourceMimetype, String targetMimetype,
            TransformationOptions options) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getMaxSourceSizeBytes(String sourceMimetype, String targetMimetype, TransformationOptions options) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ContentTransformer> getActiveTransformers(String sourceMimetype, long sourceSize, String targetMimetype,
            TransformationOptions options) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ContentTransformer> getActiveTransformers(String sourceMimetype, String targetMimetype,
            TransformationOptions options) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ContentTransformer getImageTransformer() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isTransformable(ContentReader reader, ContentWriter writer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isTransformable(ContentReader reader, ContentWriter writer, TransformationOptions options) {
        throw new UnsupportedOperationException();
    }
}
