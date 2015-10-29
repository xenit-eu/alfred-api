package eu.xenit.apix.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.xenit.apix.data.NodeRef;
import eu.xenit.apix.data.QName;
import eu.xenit.apix.node.ChildParentAssociation;
import eu.xenit.apix.node.NodeAssociations;
import eu.xenit.apix.rest.v1.nodes.NodeInfo;
import java.io.IOException;
import java.util.ArrayList;
import org.junit.Test;

/**
 * Created by Michiel Huygen on 19/05/2016.
 */
public class NodeInfoSerializationTest {

    @Test

    public void TestDeserializeNodeInfoJson() throws IOException {
        NodeInfo nodeinfo = new NodeInfo();
        nodeinfo.associations = new NodeAssociations();
        nodeinfo.associations.setParents(new ArrayList<ChildParentAssociation>());
        nodeinfo.associations.getParents().add(new ChildParentAssociation(new NodeRef("workspace://SpacesStore/7987"),
                new NodeRef("workspace://SpacesStore/7987"), new QName("hello"), false));

        new ObjectMapper().readValue(new ObjectMapper().writeValueAsString(nodeinfo), NodeInfo.class);
    }
}
