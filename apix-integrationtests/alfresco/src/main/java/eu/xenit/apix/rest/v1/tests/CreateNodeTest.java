package eu.xenit.apix.rest.v1.tests;

import eu.xenit.apix.alfresco.ApixToAlfrescoConversion;
import eu.xenit.apix.data.NodeRef;
import eu.xenit.apix.data.QName;

import eu.xenit.apix.server.ApplicationContextProvider;
import java.util.HashMap;
import eu.xenit.apix.rest.v1.nodes.CreateNodeOptions;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.httpclient.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;

public class CreateNodeTest extends NodesBaseTest {

    private NodeRef mainTestFolder;
    private NodeRef parentTestFolder;

    private ApplicationContext testApplicationContext;
    TransactionService transactionService;
    private ApixToAlfrescoConversion c;

    @Before
    public void setup() {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        // Setup the RestV1BaseTest Beans
        initialiseBeans();
        // Setup the NodesBaseTest Beans
        initializeBeansNodesBaseTest();
        // initialise the local beans
        testApplicationContext = ApplicationContextProvider.getApplicationContext();
        transactionService = (TransactionService) testApplicationContext.getBean(TransactionService.class);
        c =  (ApixToAlfrescoConversion) testApplicationContext.getBean(ApixToAlfrescoConversion.class);

        final HashMap<String, NodeRef> initializedNodeRefs = init();
        mainTestFolder = c.apix(getMainTestFolder());
        parentTestFolder = initializedNodeRefs.get(RestV1BaseTest.TESTFOLDER_NAME);
    }

    @Test
    public void testCreateFile() {
        String name = "newFile";
        CreateNodeOptions createNodeOptions = getCreateNodeOptions(parentTestFolder, name,
                c.apix(ContentModel.TYPE_CONTENT), null, null);
        NodeRef newRef = transactionService.getRetryingTransactionHelper()
                .doInTransaction(() -> {
                    return doPostNodes(createNodeOptions, HttpStatus.SC_OK, null, null);
                }, false, true);
        checkCreatedNode(newRef, createNodeOptions);
    }

    @Test
    public void testCreateFolder() {
        String name = "newFolder";
        CreateNodeOptions createNodeOptions = getCreateNodeOptions(parentTestFolder, name,
                c.apix(ContentModel.TYPE_FOLDER), null, null);
        NodeRef newRef = transactionService.getRetryingTransactionHelper()
                .doInTransaction(() -> {
                    return doPostNodes(createNodeOptions, HttpStatus.SC_OK, null, null);
                }, false, true);
        checkCreatedNode(newRef, createNodeOptions);
    }

    @Ignore
    public void testCreateFileWithNoType() {
        String name = "noType";
        CreateNodeOptions createNodeOptions = getCreateNodeOptions(parentTestFolder, name,
                null, null, null);
        //TODO : Should return SC_BAD_REQUEST
        NodeRef newRef = transactionService.getRetryingTransactionHelper()
                .doInTransaction(() -> {
                    return doPostNodes(createNodeOptions, HttpStatus.SC_INTERNAL_SERVER_ERROR, null, null);
                }, false, true);
        checkCreatedNode(newRef, createNodeOptions);
    }

    @Test
    public void testCreateFileWithProperties() {
        String name = "newFile1";
        HashMap<QName, String[]> properties = getBasicProperties();
        CreateNodeOptions createNodeOptions = getCreateNodeOptions(parentTestFolder, name,
                c.apix(ContentModel.TYPE_CONTENT), properties, null);
        NodeRef newRef = transactionService.getRetryingTransactionHelper()
                .doInTransaction(() -> {
                    return doPostNodes(createNodeOptions, HttpStatus.SC_OK, null, null);
                }, false, true);
        checkCreatedNode(newRef, createNodeOptions);
    }

    @Test
    public void testCreateFileDuplicateName() {
        String name = "duplicate";
        CreateNodeOptions createNodeOptions = getCreateNodeOptions(parentTestFolder, name,
                c.apix(ContentModel.TYPE_CONTENT), null, null);
        NodeRef newRef = transactionService.getRetryingTransactionHelper()
                .doInTransaction(() -> {
                    return doPostNodes(createNodeOptions, HttpStatus.SC_OK, null, null);
                }, false, true);
        checkCreatedNode(newRef, createNodeOptions);
        transactionService.getRetryingTransactionHelper()
                .doInTransaction(() -> {
                    return doPostNodes(createNodeOptions, HttpStatus.SC_BAD_REQUEST, null, null);
                }, false, true);
    }

    @Test
    public void testCreateNodeReturnsAccessDenied() {
        String name = "Forbidden";
        CreateNodeOptions createNodeOptions = getCreateNodeOptions(mainTestFolder, name,
                c.apix(ContentModel.TYPE_CONTENT), null, null);
        transactionService.getRetryingTransactionHelper()
                .doInTransaction(() -> {
                    doPostNodes(createNodeOptions, HttpStatus.SC_FORBIDDEN,
                            RestV1BaseTest.USERWITHOUTRIGHTS, RestV1BaseTest.USERWITHOUTRIGHTS );
                    return null;
                }, false, true);
    }

    @After
    public void cleanUp() {
        this.removeMainTestFolder();
    }

}
