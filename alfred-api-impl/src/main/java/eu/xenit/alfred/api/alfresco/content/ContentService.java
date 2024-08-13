package eu.xenit.alfred.api.alfresco.content;

import eu.xenit.alfred.api.content.IContentService;
import eu.xenit.alfred.api.data.ContentData;
import eu.xenit.alfred.api.data.ContentInputStream;
import eu.xenit.alfred.api.data.NodeRef;
import eu.xenit.alfred.api.node.INodeService;
import java.io.InputStream;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("eu.xenit.alfred.api.content.IContentService")
public class ContentService implements IContentService {

    @Autowired
    INodeService nodeService;

    private org.alfresco.service.cmr.repository.ContentService alfContentService;

    @Autowired
    public ContentService(ServiceRegistry serviceRegistry) {
        alfContentService = serviceRegistry.getContentService();
    }

    public boolean contentUrlExists(String contentUrl) {
        final String contentUrlF = contentUrl;
        final org.alfresco.service.cmr.repository.ContentService cService = alfContentService;
        return (boolean) AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Object>() {
            @Override
            public Object doWork() throws Exception {
                return cService.getRawReader(contentUrlF).exists();
            }
        });
    }

    ;

    public void setContent(NodeRef node, InputStream inputStream, String originalFilename) {
        this.nodeService.setContent(node, inputStream, originalFilename);
    }

    public ContentData createContent(InputStream inputStream, String mimeType, String encoding) {
        return this.nodeService.createContent(inputStream, mimeType, encoding);
    }

    public ContentInputStream getContent(NodeRef node) {
        return this.nodeService.getContent(node);
    }


}
