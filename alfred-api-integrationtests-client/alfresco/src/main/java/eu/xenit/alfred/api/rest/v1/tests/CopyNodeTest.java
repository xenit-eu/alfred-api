package eu.xenit.alfred.api.rest.v1.tests;

import eu.xenit.alfred.api.data.NodeRef;
import eu.xenit.alfred.api.data.QName;
import eu.xenit.alfred.api.node.INodeService;
import eu.xenit.alfred.api.rest.v1.nodes.CreateNodeOptions;
import java.util.HashMap;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.apache.commons.httpclient.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CopyNodeTest extends NodesBaseTest {

    private NodeRef mainTestFolder;
    private NodeRef copyFromFile;
    private NodeRef copyFromFolder;

    private final INodeService iNodeService;

    public CopyNodeTest() {
        iNodeService = getBean(eu.xenit.alfred.api.alfresco.metadata.NodeService.class);
    }

    @Before
    public void setup() {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

        final HashMap<String, NodeRef> initializedNodeRefs = init();
        mainTestFolder = c.alfredApi(getMainTestFolder());
        copyFromFile = initializedNodeRefs.get(RestV1BaseTest.TESTFILE_NAME);
        copyFromFolder = initializedNodeRefs.get(RestV1BaseTest.TESTFOLDER_NAME);
    }

    @Test
    public void testCopyFileNode() {
        CreateNodeOptions createNodeOptions = getCreateNodeOptions(mainTestFolder, null,
                null, null, copyFromFile);
        NodeRef newRef = transactionService.getRetryingTransactionHelper()
                .doInTransaction(() -> {
                    return doPostNodes(createNodeOptions, HttpStatus.SC_OK, null, null);
                }, false, true);
        checkCreatedNode(newRef, createNodeOptions);
    }

    @Test
    public void testCopyFileNodeWithAspectsToRemove() {
        transactionService.getRetryingTransactionHelper()
                .doInTransaction(() -> {
                    serviceRegistry.getNodeService()
                            .addAspect(c.alfresco(copyFromFile), ContentModel.ASPECT_TEMPORARY, new HashMap<>());
                    return null;
                }, false, true);

        QName[] aspectsToRemove = new QName[1];
        aspectsToRemove[0] = c.alfredApi(ContentModel.ASPECT_TEMPORARY);
        CreateNodeOptions createNodeOptions = getCreateNodeOptions(mainTestFolder, null,
                null, null, null, aspectsToRemove, copyFromFile);

        NodeRef newRef = transactionService.getRetryingTransactionHelper()
                .doInTransaction(() -> {
                    return doPostNodes(createNodeOptions, HttpStatus.SC_OK, null, null);
                }, false, true);
        checkCreatedNode(newRef, createNodeOptions);
    }

    @Test
    public void testCopyFolderNode() {
        CreateNodeOptions createNodeOptions = getCreateNodeOptions(mainTestFolder, null,
                null, null, copyFromFolder);
        NodeRef newRef = transactionService.getRetryingTransactionHelper()
                .doInTransaction(() -> {
                    return doPostNodes(createNodeOptions, HttpStatus.SC_OK, null, null);
                }, false, true);
        checkCreatedNode(newRef, createNodeOptions);
    }

    @Test
    public void testCopyFileWithName() {
        final String newName = "Copy";
        CreateNodeOptions createNodeOptions = getCreateNodeOptions(mainTestFolder, newName,
                null, null, copyFromFile);
        NodeRef newRef = transactionService.getRetryingTransactionHelper()
                .doInTransaction(() -> {
                    return doPostNodes(createNodeOptions, HttpStatus.SC_OK, null, null);
                }, false, true);
        checkCreatedNode(newRef, createNodeOptions);
    }

    @Test
    public void testCopyFolderWithName() {
        final String newName = "CopiedFolder";
        CreateNodeOptions createNodeOptions = getCreateNodeOptions(mainTestFolder, newName, null,
                null, copyFromFolder);
        NodeRef newRef = transactionService.getRetryingTransactionHelper()
                .doInTransaction(() -> {
                    return doPostNodes(createNodeOptions, HttpStatus.SC_OK, null, null);
                }, false, true);
        checkCreatedNode(newRef, createNodeOptions);
    }

    @Test
    public void testCopyFileDuplicateName() {
        final String duplicateName = "duplicateName";
        CreateNodeOptions createNodeOptions = getCreateNodeOptions(mainTestFolder, duplicateName, null,
                null, copyFromFile);
        //First copy should succeed
        NodeRef newRef = transactionService.getRetryingTransactionHelper()
                .doInTransaction(() -> {
                    return doPostNodes(createNodeOptions, HttpStatus.SC_OK, null, null);
                }, false, true);
        checkCreatedNode(newRef, createNodeOptions);

        //Second copy should fail
        transactionService.getRetryingTransactionHelper()
                .doInTransaction(() -> {
                    doPostNodes(createNodeOptions, HttpStatus.SC_BAD_REQUEST,
                            null, null);
                    return null;
                }, false, true);
    }

    @Test
    public void testCopyFolderDuplicateName() {
        final NodeRef childRef = iNodeService.getChildAssociations(mainTestFolder).get(0).getTarget();
        final String newName = iNodeService.getMetadata(childRef).getProperties().get(c.alfredApi(ContentModel.PROP_NAME))
                .get(0);
        CreateNodeOptions createNodeOptions = getCreateNodeOptions(mainTestFolder, newName,
                null, null, copyFromFolder);
        NodeRef newRef = transactionService.getRetryingTransactionHelper()
                .doInTransaction(() -> {
                    return doPostNodes(createNodeOptions, HttpStatus.SC_BAD_REQUEST,
                            null, null);
                }, false, true);
    }

    @Test
    public void testCopyNodeWithProperties() {
        final String newName = "NewName";
        HashMap<QName, String[]> properties = getBasicProperties();
        CreateNodeOptions createNodeOptions = getCreateNodeOptions(mainTestFolder, newName,
                null, properties, copyFromFile);
        NodeRef newRef = transactionService.getRetryingTransactionHelper()
                .doInTransaction(() -> {
                    return doPostNodes(createNodeOptions, HttpStatus.SC_OK, null, null);
                }, false, true);
        checkCreatedNode(newRef, createNodeOptions);
    }

    @Test
    public void testCopyNodeReturnsAccesDenied() {
        CreateNodeOptions createNodeOptions = getCreateNodeOptions(mainTestFolder, null,
                null, null, copyFromFile);
        transactionService.getRetryingTransactionHelper()
                .doInTransaction(() -> {
                    doPostNodes(createNodeOptions, HttpStatus.SC_FORBIDDEN,
                            RestV1BaseTest.USERWITHOUTRIGHTS, RestV1BaseTest.USERWITHOUTRIGHTS);
                    return null;
                }, false, true);
    }

    @Test
    public void testCopyFolderInception() {
        CreateNodeOptions createNodeOptions = getCreateNodeOptions(copyFromFolder, null,
                null, null, copyFromFolder);
        transactionService.getRetryingTransactionHelper()
                .doInTransaction(() -> {
                    doPostNodes(createNodeOptions, HttpStatus.SC_OK, null, null);
                    return null;
                }, false, true);
    }

    @Test
    public void testCopyFolderTypeChange() {
        CreateNodeOptions createNodeOptions = getCreateNodeOptions(mainTestFolder, null,
                c.alfredApi(ContentModel.TYPE_CONTENT), null, copyFromFolder);
        transactionService.getRetryingTransactionHelper()
                .doInTransaction(() -> {
                    doPostNodes(createNodeOptions, HttpStatus.SC_INTERNAL_SERVER_ERROR, null, null);
                    return null;
                }, false, true);
    }

    @Test
    public void testCopyFolderSubTyping() {
        CreateNodeOptions createNodeOptions = getCreateNodeOptions(mainTestFolder, null,
                c.alfredApi(ContentModel.TYPE_DICTIONARY_MODEL), null, copyFromFile);
        transactionService.getRetryingTransactionHelper()
                .doInTransaction(() -> {
                    doPostNodes(createNodeOptions, HttpStatus.SC_OK, null, null);
                    return null;
                }, false, true);
    }

    @Test
    public void testMultipleCopies() {
        CreateNodeOptions createNodeOptions = getCreateNodeOptions(mainTestFolder, null,
                null, null, copyFromFolder);
        for (int i = 0; i < 5; i++) {
            NodeRef newRef = transactionService.getRetryingTransactionHelper()
                    .doInTransaction(() -> {
                        return doPostNodes(createNodeOptions, HttpStatus.SC_OK, null, null);
                    }, false, true);
            checkCreatedNode(newRef, createNodeOptions);
        }
    }

    @After
    public void cleanUp() {
        this.removeMainTestFolder();
    }

}
