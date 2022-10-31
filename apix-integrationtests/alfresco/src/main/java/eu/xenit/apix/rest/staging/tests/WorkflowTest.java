package eu.xenit.apix.rest.staging.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskDefinition;
import org.alfresco.service.namespace.QName;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class WorkflowTest extends StagingBaseTest {

    private final static Logger logger = LoggerFactory.getLogger(WorkflowTest.class);
    private final List<WorkflowPath> wfPaths = new ArrayList<>();
    @Autowired
    private ServiceRegistry serviceRegistry;
    private WorkflowService workflowService;
    private AuthorityService authorityService;

    @Before
    public void setup() {
        this.cleanUp();
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

        this.workflowService = this.serviceRegistry.getWorkflowService();
        this.authorityService = this.serviceRegistry.getAuthorityService();

        this.serviceRegistry.getRetryingTransactionHelper()
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
                    @Override
                    public Object execute() throws Throwable {
                        WorkflowPath wfPath = createWorkflow();
                        wfPaths.add(wfPath);
                        return null;
                    }
                }, false, true);
    }

    private WorkflowPath createWorkflow() {
        logger.debug("listing workflow definitions");
        List<WorkflowDefinition> workflowDefs = this.workflowService.getAllDefinitions();
        for (WorkflowDefinition workflowDef : workflowDefs) {
            logger.debug("DEFINITION");
            logger.debug("description: " + workflowDef.getDescription());
            logger.debug("id: " + workflowDef.getId());
            logger.debug("name: " + workflowDef.getName());
            logger.debug("title: " + workflowDef.getTitle());
            WorkflowTaskDefinition startTaskDefinition = workflowDef.getStartTaskDefinition();
            logger.debug("startTaskDefinition metadata: " + startTaskDefinition.getMetadata().toString());
            logger.debug("startTaskDefinition: " + startTaskDefinition.toString());
            logger.debug("##################################");
        }

        WorkflowDefinition wfDefinition = this.workflowService.getDefinitionByName("activiti$activitiReviewPooled");
        if (wfDefinition == null) {
            logger.debug("wfDefinition is null");
        }

        logger.debug("Creating parameters");
        Map<QName, Serializable> parameters = new HashMap<QName, Serializable>();
        NodeRef workflowNodeRef = workflowService.createPackage(null);
        parameters.put(WorkflowModel.ASSOC_PACKAGE, workflowNodeRef);
        parameters.put(WorkflowModel.PROP_WORKFLOW_DESCRIPTION, "This is the description");
        parameters.put(WorkflowModel.PROP_DESCRIPTION, "This is the task description");
        parameters.put(WorkflowModel.ASSOC_GROUP_ASSIGNEE,
                authorityService.getAuthorityNodeRef("GROUP_" + "ALFRESCO_ADMINISTRATORS"));
        parameters.put(WorkflowModel.ASSOC_POOLED_ACTORS,
                authorityService.getAuthorityNodeRef("GROUP_" + "ALFRESCO_ADMINISTRATORS"));
        parameters.put(WorkflowModel.PROP_PERCENT_COMPLETE, 50);
        parameters.put(WorkflowModel.PROP_REASSIGNABLE, true);
        logger.debug("Done creating parameters. Starting workflow now.");
        WorkflowPath wfPath = workflowService.startWorkflow(wfDefinition.getId(), parameters);
        logger.debug("Starting workflow successful");

        return wfPath;
    }

    @Test
    public void testClaimWorkflow() throws IOException {
        logger.debug("testClaimWorkflow started");

        String currentUserName = this.authenticationService.getCurrentUserName();
        List<WorkflowTask> wfTasks = this.workflowService.getPooledTasks(currentUserName);
        assertNotEquals(0, wfTasks.size());
        WorkflowTask wfTask = wfTasks.get(0);
        Boolean claimable = workflowService.isTaskClaimable(wfTask, currentUserName, true);
        logger.debug("claimable: " + claimable);
        assertTrue(claimable);

        String url = createApixUrl("/tasks/claim");
        logger.debug(" URL: " + url);

        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost(url);

        httppost.setEntity(new StringEntity(json(String.format("{\n" +
                "\t\"id\" : \"%s\",\n" +
                "\t\"userName\" : \"admin\"\n" +
                "}", wfTask.getId()))));

        try (CloseableHttpResponse response = httpclient.execute(httppost)) {
            String jsonString = EntityUtils.toString(response.getEntity());
            logger.debug(" Result: " + jsonString + " ");
            assertEquals(200, response.getStatusLine().getStatusCode());
        }
    }

    @Test
    public void testReleaseWorkflow() throws IOException {
        logger.debug("testReleaseWorkflow started");

        final String currentUserName = this.authenticationService.getCurrentUserName();
        List<WorkflowTask> wfTasks = this.workflowService.getPooledTasks(currentUserName);
        assertNotEquals(0, wfTasks.size());
        final WorkflowTask wfTask = wfTasks.get(0);

        this.serviceRegistry.getRetryingTransactionHelper()
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
                    @Override
                    public Object execute() throws Throwable {
                        Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
                        properties.put(ContentModel.PROP_OWNER, currentUserName);
                        workflowService.updateTask(wfTask.getId(), properties, null, null);
                        return null;
                    }
                }, false, true);

        Boolean releasable = workflowService.isTaskReleasable(wfTask, currentUserName, true);
        logger.debug("releasable: " + releasable);
        assertTrue(releasable);

        String url = createApixUrl("/tasks/release");
        logger.debug(" URL: " + url);

        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost(url);

        httppost.setEntity(new StringEntity(json(String.format("{\n" +
                "\t\"id\" : \"%s\"\n" +
                "}", wfTask.getId()))));

        try (CloseableHttpResponse response = httpclient.execute(httppost)) {
            String jsonString = EntityUtils.toString(response.getEntity());
            logger.debug(" Result: " + jsonString + " ");
            assertEquals(200, response.getStatusLine().getStatusCode());
        }
    }

    @After
    public void cleanUp() {
        this.removeMainTestFolder();
        logger.debug("cleaning up workflows");
        for (WorkflowPath wfPath : this.wfPaths) {
            logger.debug("CLeaning workflow with id " + wfPath.getId());
            this.workflowService.deleteWorkflow(wfPath.getId());
        }
        wfPaths.clear();
        logger.debug("finished cleaning up workflows");
    }
}
