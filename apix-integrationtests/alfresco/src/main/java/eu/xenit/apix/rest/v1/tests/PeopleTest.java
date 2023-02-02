package eu.xenit.apix.rest.v1.tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.xenit.apix.alfresco.ApixToAlfrescoConversion;
import eu.xenit.apix.data.NodeRef;
import eu.xenit.apix.people.Person;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Request;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Created by Jasperhilven on 25-Oct-16.
 */
public class PeopleTest extends RestV1BaseTest {

    private final static Logger logger = LoggerFactory.getLogger(VersionHistoryTest.class);
    @Autowired
    @Qualifier("TransactionService")
    TransactionService transactionService;
    @Autowired
    private PersonService alfrescoPersonService;
    @Autowired
    private ApixToAlfrescoConversion c;

    @Before
    public void setup() {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
    }

    @Test
    public void testGetPerson() throws IOException {

        final HashMap<QName, Serializable> props = new HashMap<QName, Serializable>();

        final String userName = "UserNameServiceTest";
        final String email = "Email";
        final String firstName = "FirstName";
        final String lastName = "LastName";
        final PeopleTest self = this;
        props.put(ContentModel.PROP_EMAIL, email);
        props.put(ContentModel.PROP_FIRSTNAME, firstName);
        props.put(ContentModel.PROP_LASTNAME, lastName);
        props.put(ContentModel.PROP_USERNAME, userName);

        final NodeRef[] nodeRef = new NodeRef[1];
        transactionService.getRetryingTransactionHelper()
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
                    @Override
                    public Object execute() throws Throwable {
                        self.alfrescoPersonService.deletePerson(userName);
                        return null;
                    }
                }, false, true);

        transactionService.getRetryingTransactionHelper()
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
                    @Override
                    public Object execute() throws Throwable {
                        nodeRef[0] = c.apix(self.alfrescoPersonService.createPerson(props));
                        return null;
                    }
                }, false, true);

        String url = createApixUrl("/people/%s/%s/%s", nodeRef[0].getStoreRefProtocol(), nodeRef[0].getStoreRefId(),
                nodeRef[0].getGuid());

        HttpResponse httpResponse = Request.Get(url).execute().returnResponse();
        HttpEntity entity = httpResponse.getEntity();
        String result = EntityUtils.toString(entity);
        //Result from log: {"nodeRef":"workspace://SpacesStore/8f84fcf2-f516-4666-8ee8-556235875f83","userName":"UserNameServiceTest","firstName":"FirstName","lastName":"LastName","emailAddress":"Email"}
        Person person = new ObjectMapper().readValue(result, Person.class);
        Assert.assertEquals(email, person.getEmailAddress());
        Assert.assertEquals(userName, person.getUserName());
        Assert.assertEquals(firstName, person.getFirstName());
        Assert.assertEquals(lastName, person.getLastName());
    }

    @Test
    public void testGetNonExistentPerson() throws IOException {
        final String userName = "NonexistentUsername";
        String url = createApixUrl("/people/%s", userName);

        HttpResponse httpResponse = Request.Get(url).execute().returnResponse();
        Assert.assertEquals(httpResponse.getStatusLine().getStatusCode(), HttpStatus.SC_NOT_FOUND);

    }

    @Test
    public void testGetSelf() throws IOException {
        final String userName = "-me-";
        String url = createApixUrl("/people?userName=%s", userName);
        HttpResponse httpResponse = Request.Get(url).execute().returnResponse();
        String result = EntityUtils.toString(httpResponse.getEntity());
        Person person = new ObjectMapper().readValue(result, Person.class);
        Assert.assertEquals("admin@alfresco.com", person.getEmailAddress());
        Assert.assertEquals("admin", person.getUserName());
        Assert.assertEquals("Administrator", person.getFirstName());
        Assert.assertEquals("", person.getLastName());
    }

    @After
    public void cleanUp() {
        this.removeMainTestFolder();
    }
}
