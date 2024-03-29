package eu.xenit.apix.rest.v1.tests;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.xenit.apix.alfresco.ApixToAlfrescoConversion;
import eu.xenit.apix.alfresco.metadata.NodeService;
import eu.xenit.apix.data.NodeRef;
import eu.xenit.apix.data.QName;
import eu.xenit.apix.rest.v1.nodes.CreateNodeOptions;
import eu.xenit.apix.rest.v1.nodes.NodeInfo;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class NodesBaseTest extends RestV1BaseTest {

    @Autowired
    NodeService nodeService;

    @Autowired
    ApixToAlfrescoConversion c;

    protected String getSimpleNodesUrl() {
        return makeAlfrescoBaseurlAdmin() + "/apix/" + getVersion() + "/nodes";
    }

    protected String getSimpleNodesUrl(String userName, String passWord) {
        return makeAlfrescoBaseurl(userName, passWord) + "/apix/" + getVersion() + "/nodes";
    }

    protected HashMap<QName, String[]> getBasicProperties() {
        HashMap<QName, String[]> properties = new HashMap<>();
        properties.put(c.apix(ContentModel.PROP_TITLE), new String[]{"NewTitle"});
        properties.put(c.apix(ContentModel.PROP_DESCRIPTION), new String[]{"TestDescription"});
        return properties;
    }

    public eu.xenit.apix.data.NodeRef doPostNodes(CreateNodeOptions createNodeOptions, int expectedResponseCode, String username, String password) throws Throwable {
        // If username || password is null, admin account is used
        final String url = (username == null || password == null ) ? getSimpleNodesUrl() : getSimpleNodesUrl(username, password);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String requestBody = objectMapper.writeValueAsString(createNodeOptions);

        //Jackson setup needs to be fixed to use full deserialization
        NodeInfo nodeInfo = null; //doPostExpected(url, NodeInfo.class, requestBody, expectedResponseCode);

        if (nodeInfo != null) {
            //deserialization succeeded
            return nodeInfo.getNoderef();
        } else {
            //deserialization failed
            HttpEntityEnclosingRequestBase req = new HttpPost(url);
            final CloseableHttpClient checkoutHttpclient = HttpClients.createDefault();
            req.setEntity(new StringEntity(requestBody, ContentType.APPLICATION_JSON));
            try (CloseableHttpResponse response = checkoutHttpclient.execute(req)) {
                String result = EntityUtils.toString(response.getEntity());
                assertEquals(expectedResponseCode, response.getStatusLine().getStatusCode());

                if (expectedResponseCode == HttpStatus.SC_OK) {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode jsonNode = mapper.readTree(result);
                    return new eu.xenit.apix.data.NodeRef(jsonNode.get("noderef").asText());
                } else {
                    return null;
                }
            }
        }
    }

    protected CreateNodeOptions getCreateNodeOptions(eu.xenit.apix.data.NodeRef parentRef, String name,
            eu.xenit.apix.data.QName type, HashMap<QName, String[]> properties, QName[] aspectsToAdd,
            QName[] aspectsToRemove, eu.xenit.apix.data.NodeRef copyFrom) {
        String parentRefString = (parentRef != null) ? parentRef.toString() : null;
        String copyFromString = (copyFrom != null) ? copyFrom.toString() : null;
        String typeString = (type != null) ? type.toString() : null;

        return new CreateNodeOptions(parentRefString, name, typeString, properties, aspectsToAdd, aspectsToRemove, copyFromString);
    }

    protected CreateNodeOptions getCreateNodeOptions(eu.xenit.apix.data.NodeRef parentRef,
            String name, eu.xenit.apix.data.QName type, HashMap<QName, String[]> properties,
            eu.xenit.apix.data.NodeRef copyFrom) {
        return getCreateNodeOptions(parentRef, name, type, properties, null, null, copyFrom);
    }

    public void checkCreatedNode(NodeRef newRef, CreateNodeOptions createNodeOptions) {
        assertTrue(nodeService.exists(newRef));
        assertEquals(createNodeOptions.getParent(), nodeService.getParentAssociations(newRef).get(0).getTarget().toString());

        if (createNodeOptions.getType() != null) {
            assertEquals(createNodeOptions.getType(), nodeService.getMetadata(newRef).getType().toString());
        }

        if (createNodeOptions.getCopyFrom() != null) {
            assertTrue(nodeService.exists(new NodeRef(createNodeOptions.getCopyFrom())));
        }

        if (createNodeOptions.getProperties() != null) {
            for (Map.Entry<QName, String[]> property : createNodeOptions.getProperties().entrySet()) {
                assertArrayEquals(property.getValue(), nodeService.getMetadata(newRef).getProperties().get(property.getKey()).toArray());
            }
        }

        if (createNodeOptions.getAspectsToAdd() != null) {
            for (QName aspect : createNodeOptions.getAspectsToAdd()) {
                assertNotNull(nodeService.getMetadata(newRef).getAspects()
                        .stream()
                        .filter(testAspect -> testAspect.equals(aspect))
                        .findFirst()
                        .orElse(null));
            }
        }

        if (createNodeOptions.getAspectsToRemove() != null) {
            for (QName aspect : createNodeOptions.getAspectsToRemove()) {
                assertNull(nodeService.getMetadata(newRef).getAspects()
                        .stream()
                        .filter(testAspect -> testAspect.equals(aspect))
                        .findFirst()
                        .orElse(null));
            }
        }
    }

}
