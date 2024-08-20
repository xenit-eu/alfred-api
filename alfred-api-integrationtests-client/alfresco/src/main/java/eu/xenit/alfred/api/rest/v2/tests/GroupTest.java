package eu.xenit.alfred.api.rest.v2.tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.xenit.alfred.api.people.Person;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GroupTest extends RestV2BaseTest {

    private final String[] userNames = {"GroupTestUser", "GroupTestUser2", "GroupTestUser3"};
    private final String[] emails = {"email@example.com", "email2@example.com", "email3@example.com"};
    private final String[] firstNames = {"FirstName", "FirstName2", "FirstName3"};
    private final String[] lastNames = {"LastName", "LastName2", "LastName3"};

    private final String groupName = "GroupTestGroup";
    private final String groupIdentifier = "GROUP_" + groupName;

    private final PersonService alfrescoPersonService;
    private final AuthorityService alfrescoAuthorityService;

    public GroupTest() {
        alfrescoPersonService = serviceRegistry.getPersonService();
        alfrescoAuthorityService = serviceRegistry.getAuthorityService();
    }

    @Before
    public void setup() {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

        // Create 3 dummy users
        final List<HashMap<QName, Serializable>> propList = new ArrayList<HashMap<QName, Serializable>>();

        final GroupTest self = this;
        for (int i = 0; i < 3; i++) {
            HashMap<QName, Serializable> props = new HashMap<QName, Serializable>();
            props.put(ContentModel.PROP_EMAIL, emails[i]);
            props.put(ContentModel.PROP_FIRSTNAME, firstNames[i]);
            props.put(ContentModel.PROP_LASTNAME, lastNames[i]);
            props.put(ContentModel.PROP_USERNAME, userNames[i]);
            propList.add(props);
        }

        System.out.println("First transaction");

        transactionService.getRetryingTransactionHelper()
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
                    @Override
                    public Object execute() throws Throwable {
                        for (int i = 0; i < 3; i++) {
                            if (self.alfrescoAuthorityService.authorityExists(userNames[i])) {
                                self.alfrescoPersonService.deletePerson(userNames[i]);
                            }
                        }
                        return null;
                    }
                }, false, true);

        System.out.println("Second transaction");

        transactionService.getRetryingTransactionHelper()
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
                    @Override
                    public Object execute() throws Throwable {
                        for (int i = 0; i < 3; i++) {
                            c.alfredApi(self.alfrescoPersonService.createPerson(propList.get(i)));
                        }
                        return null;
                    }
                }, false, true);

        System.out.println("Third transaction");

        // Create dummy group

        transactionService.getRetryingTransactionHelper()
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
                    @Override
                    public Object execute() throws Throwable {
                        if (self.alfrescoAuthorityService.authorityExists(groupIdentifier)) {
                            self.alfrescoAuthorityService.deleteAuthority(groupIdentifier);
                        }
                        return null;
                    }
                }, false, true);

        System.out.println("Fourth transaction");

        transactionService.getRetryingTransactionHelper()
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {
                    @Override
                    public Object execute() throws Throwable {
                        self.alfrescoAuthorityService.createAuthority(AuthorityType.GROUP, groupName);
                        return null;
                    }
                }, false, true);

        System.out.println("Transactions complete");
    }

    @Test
    public void testNoUsersInGroup() throws IOException {
        String url = createAlfredApiUrl("/groups/%s/people?immediate=true", groupIdentifier);
        System.out.println("About to http get");
        HttpResponse httpResponse = Request.Get(url).execute().returnResponse();
        HttpEntity entity = httpResponse.getEntity();
        String result = EntityUtils.toString(entity);
        System.out.println(result);
        Person[] persons = new ObjectMapper().readValue(result, Person[].class);
        Assert.assertEquals(0, persons.length);
    }

    @Test
    public void testAddUserToGroup() throws IOException {
        String putUrl = createAlfredApiUrl("/groups/%s/people", groupIdentifier);
        String getUrl = createAlfredApiUrl("/groups/%s/people?immediate=true", groupIdentifier);
        System.out.println("About to http put");
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPut put = new HttpPut(putUrl);
        put.setEntity(
                new StringEntity(String.format("{\"users\":[\"%s\"]}", userNames[0]), ContentType.APPLICATION_JSON));
        CloseableHttpResponse respOne = client.execute(put);
        Assert.assertEquals(200, respOne.getStatusLine().getStatusCode());

        HttpResponse respTwo = Request.Get(getUrl).execute().returnResponse();
        HttpEntity entity = respTwo.getEntity();
        String result = EntityUtils.toString(entity);
        System.out.println(result);
        Person[] persons = new ObjectMapper().readValue(result, Person[].class);
        Assert.assertEquals(1, persons.length);
        Assert.assertEquals(emails[0], persons[0].getEmailAddress());
        Assert.assertEquals(userNames[0], persons[0].getUserName());
        Assert.assertEquals(firstNames[0], persons[0].getFirstName());
        Assert.assertEquals(lastNames[0], persons[0].getLastName());
    }


    @Test
    public void testReplaceUsersInGroup() throws IOException {
        String putUrl = createAlfredApiUrl("/groups/%s/people", groupIdentifier);
        String getUrl = createAlfredApiUrl("/groups/%s/people?immediate=true", groupIdentifier);

        // Add first user

        System.out.println("About to http put");
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPut put = new HttpPut(putUrl);
        put.setEntity(
                new StringEntity(String.format("{\"users\":[\"%s\"]}", userNames[0]), ContentType.APPLICATION_JSON));
        CloseableHttpResponse respOne = client.execute(put);
        Assert.assertEquals(200, respOne.getStatusLine().getStatusCode());

        // Replace first user with 2 new ones

        put = new HttpPut(putUrl);
        put.setEntity(new StringEntity(String.format("{\"users\":[\"%s\",\"%s\"]}", userNames[1], userNames[2]),
                ContentType.APPLICATION_JSON));

        CloseableHttpResponse respTwo = client.execute(put);
        Assert.assertEquals(200, respOne.getStatusLine().getStatusCode());

        // Verify results

        HttpResponse respThree = Request.Get(getUrl).execute().returnResponse();
        HttpEntity entity = respThree.getEntity();
        String result = EntityUtils.toString(entity);
        System.out.println(result);
        Person[] persons = new ObjectMapper().readValue(result, Person[].class);
        Assert.assertEquals(2, persons.length);
        Assert.assertEquals(emails[1], persons[0].getEmailAddress());
        Assert.assertEquals(userNames[1], persons[0].getUserName());
        Assert.assertEquals(firstNames[1], persons[0].getFirstName());
        Assert.assertEquals(lastNames[1], persons[0].getLastName());
        Assert.assertEquals(emails[2], persons[1].getEmailAddress());
        Assert.assertEquals(userNames[2], persons[1].getUserName());
        Assert.assertEquals(firstNames[2], persons[1].getFirstName());
        Assert.assertEquals(lastNames[2], persons[1].getLastName());
    }

    @Test
    public void testReplaceUsersWithSame() throws IOException {
        String putUrl = createAlfredApiUrl("/groups/%s/people", groupIdentifier);
        String getUrl = createAlfredApiUrl("/groups/%s/people?immediate=true", groupIdentifier);

        // Add first user

        System.out.println("About to http put");
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPut put = new HttpPut(putUrl);
        put.setEntity(
                new StringEntity(String.format("{\"users\":[\"%s\"]}", userNames[0]), ContentType.APPLICATION_JSON));
        CloseableHttpResponse respOne = client.execute(put);
        Assert.assertEquals(200, respOne.getStatusLine().getStatusCode());

        // Replace first user with itself (aka run put again)

        CloseableHttpResponse respTwo = client.execute(put);
        Assert.assertEquals(200, respOne.getStatusLine().getStatusCode());

        // Verify results

        HttpResponse respThree = Request.Get(getUrl).execute().returnResponse();
        HttpEntity entity = respThree.getEntity();
        String result = EntityUtils.toString(entity);
        System.out.println(result);
        Person[] persons = new ObjectMapper().readValue(result, Person[].class);
        Assert.assertEquals(1, persons.length);
        Assert.assertEquals(emails[0], persons[0].getEmailAddress());
        Assert.assertEquals(userNames[0], persons[0].getUserName());
        Assert.assertEquals(firstNames[0], persons[0].getFirstName());
        Assert.assertEquals(lastNames[0], persons[0].getLastName());
    }

    @Test
    public void testReplaceUsersWithSuperSet() throws IOException {
        String putUrl = createAlfredApiUrl("/groups/%s/people", groupIdentifier);
        String getUrl = createAlfredApiUrl("/groups/%s/people?immediate=true", groupIdentifier);

        // Add first user

        System.out.println("About to http put");
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPut put = new HttpPut(putUrl);
        put.setEntity(
                new StringEntity(String.format("{\"users\":[\"%s\"]}", userNames[0]), ContentType.APPLICATION_JSON));
        CloseableHttpResponse respOne = client.execute(put);
        Assert.assertEquals(200, respOne.getStatusLine().getStatusCode());

        // Replace first user with 1 same and 1 new

        put = new HttpPut(putUrl);
        put.setEntity(new StringEntity(String.format("{\"users\":[\"%s\",\"%s\"]}", userNames[1], userNames[0]),
                ContentType.APPLICATION_JSON));

        CloseableHttpResponse respTwo = client.execute(put);
        Assert.assertEquals(200, respOne.getStatusLine().getStatusCode());

        // Verify results

        HttpResponse respThree = Request.Get(getUrl).execute().returnResponse();
        HttpEntity entity = respThree.getEntity();
        String result = EntityUtils.toString(entity);
        System.out.println(result);
        Person[] persons = new ObjectMapper().readValue(result, Person[].class);
        Assert.assertEquals(2, persons.length);
        Assert.assertEquals(emails[0], persons[0].getEmailAddress());
        Assert.assertEquals(userNames[0], persons[0].getUserName());
        Assert.assertEquals(firstNames[0], persons[0].getFirstName());
        Assert.assertEquals(lastNames[0], persons[0].getLastName());
        Assert.assertEquals(emails[1], persons[1].getEmailAddress());
        Assert.assertEquals(userNames[1], persons[1].getUserName());
        Assert.assertEquals(firstNames[1], persons[1].getFirstName());
        Assert.assertEquals(lastNames[1], persons[1].getLastName());
    }

    @Test
    public void testClearUsers() throws IOException {
        String putUrl = createAlfredApiUrl("/groups/%s/people", groupIdentifier);
        String getUrl = createAlfredApiUrl("/groups/%s/people?immediate=true", groupIdentifier);

        // Add two users

        System.out.println("About to http put");
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPut put = new HttpPut(putUrl);
        put.setEntity(new StringEntity(String.format("{\"users\":[\"%s\",\"%s\"]}", userNames[0], userNames[1]),
                ContentType.APPLICATION_JSON));
        CloseableHttpResponse respOne = client.execute(put);
        Assert.assertEquals(200, respOne.getStatusLine().getStatusCode());

        // Replace with empty list

        put = new HttpPut(putUrl);
        put.setEntity(new StringEntity("{\"users\":[]}", ContentType.APPLICATION_JSON));

        CloseableHttpResponse respTwo = client.execute(put);
        Assert.assertEquals(200, respOne.getStatusLine().getStatusCode());

        // Verify results

        HttpResponse respThree = Request.Get(getUrl).execute().returnResponse();
        HttpEntity entity = respThree.getEntity();
        String result = EntityUtils.toString(entity);
        System.out.println(result);
        Person[] persons = new ObjectMapper().readValue(result, Person[].class);
        Assert.assertEquals(0, persons.length);
    }
}
