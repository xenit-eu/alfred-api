package eu.xenit.apix.rest.v1.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import eu.xenit.apix.data.NodeRef;
import eu.xenit.apix.node.ChildParentAssociation;
import eu.xenit.apix.node.INodeService;
import java.io.IOException;
import java.util.List;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Created by kenneth on 16.03.16.
 */
public class CreateNodeTest extends BaseTest {

    public static final String TITLE_VALUE = "newTitle";
    @Autowired
    INodeService nodeService;

    @Autowired
    @Qualifier("NodeService")
    NodeService alfrescoNodeService;

    @Autowired
    @Qualifier("TransactionService")
    TransactionService transactionService;

    @Before
    public void setup() {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
    }

    @Test
    public void testCreateNode() throws IOException {
        final NodeRef[] nodeRef = init();
        List<ChildParentAssociation> parentAssociations = this.nodeService.getParentAssociations(nodeRef[0]);
        final ChildParentAssociation primaryParentAssoc = (ChildParentAssociation) parentAssociations.get(0);
        assertTrue(primaryParentAssoc.isPrimary());

        final String url = makeAlfrescoBaseurl("admin", "admin") + "/apix/v1/nodes";
        final CloseableHttpClient httpclient = HttpClients.createDefault();

        String responseBody = transactionService.getRetryingTransactionHelper()
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<String>() {
                    @Override
                    public String execute() throws Throwable {
                        HttpPost httppost = new HttpPost(url);
                        String jsonString = json(String.format(
                                "{" +
                                        "\"parent\":\"%s\"," +
                                        "\"name\":\"createNode\"," +
                                        "\"properties\":{" +
                                        "'%s' : ['" + TITLE_VALUE + "']" +
                                        "}," +
                                        "\"type\":\"%s\"" +
                                        "}"
                                , primaryParentAssoc.getTarget().toString(), ContentModel.PROP_TITLE.toString(),
                                ContentModel.TYPE_CONTENT.toString()));
                        httppost.setEntity(new StringEntity(jsonString));
                        List<ChildParentAssociation> childAssociations = nodeService
                                .getChildAssociations(primaryParentAssoc.getTarget());
                        assertEquals(childAssociations.size(), 1);
                        try (CloseableHttpResponse response = httpclient.execute(httppost)) {
//                    String responseString = EntityUtils.toString(response.getEntity());
                            assertEquals(200, response.getStatusLine().getStatusCode());
//                    return responseString;
                        }
                        return null;
                    }
                }, false, true);

        NodeRef parent = primaryParentAssoc.getTarget();
//         FIXME : Test that METADATA has been filled
//        ObjectMapper mapper = new ObjectMapper();
//        NodeMetadata nodeMetadata =  mapper.readValue(responseBody, new TypeReference<NodeMetadata>() {});
//        String nodeTitle = nodeMetadata.properties.get(new QName(ContentModel.PROP_TITLE.toString())).get(0);
        List<ChildParentAssociation> newChildAssocs = nodeService.getChildAssociations(parent);
        assertEquals(2, newChildAssocs.size());
//        assertEquals(TITLE_VALUE, nodeTitle);
    }

    @After
    public void cleanUp() {
        this.removeMainTestFolder();
    }
}
