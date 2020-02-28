package eu.xenit.apix.content;

import eu.xenit.apix.data.ContentData;
import eu.xenit.apix.data.ContentInputStream;

import java.io.InputStream;

/**
 * Created by jasper on 25/10/17.
 */
public interface IContentService {

    /**
     * Checks if the content url exists.
     * @param contentUrl The content url to check.
     * @return true if exists, false otherwise
     */
    public boolean contentUrlExists(String contentUrl);

    /**
     * Sets content of a specific node. Also changes mimetype by guessing it via the original file name.
     *
     * @param node NodeRef of the node where the content of the inputStream will placed.
     * @param inputStream The input stream that contains the content. In case the input stream is null the content will
     * be set to empty.
     * @param originalFilename The filename of the content. This is only used to guess the mimetype of the node.
     */
    void setContent(eu.xenit.apix.data.NodeRef node, InputStream inputStream, String originalFilename);

    /**
     * Creates a content without linking it to a specific node yet.
     *
     * @param inputStream Can be null: in that case, the content property of the file is remove.
     * @param mimeType ex: "application/pdf"
     * @param encoding ex: "UTF-8", ....
     * @return the URL of the content (can be set to the content property of a node).
     */
    ContentData createContent(InputStream inputStream, String mimeType, String encoding);

    /**
     * Returns content as inputStream + other related informations (mimeType, size, ...)
     *
     * @param node NodeRef from where the content will be gathered.
     * @return ContentInputStream.inputStream has to be closed!
     */
    ContentInputStream getContent(eu.xenit.apix.data.NodeRef node);
}
