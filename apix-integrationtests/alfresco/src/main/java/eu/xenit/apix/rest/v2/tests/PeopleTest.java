package eu.xenit.apix.rest.v2.tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.xenit.apix.alfresco.ApixToAlfrescoConversion;
import eu.xenit.apix.people.Person;
import eu.xenit.apix.server.ApplicationContextProvider;
import java.io.IOException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.transaction.TransactionService;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PeopleTest extends RestV2BaseTest {
    @Test
    public void testGetSelf() throws IOException {
        final String userName = "-me-";
        String url = createApixUrl("/people/%s", userName);
        HttpResponse httpResponse = Request.Get(url).execute().returnResponse();
        String result = EntityUtils.toString(httpResponse.getEntity());
        Person person = new ObjectMapper().readValue(result, Person.class);
        Assert.assertEquals("admin@alfresco.com", person.getEmailAddress());
        Assert.assertEquals("admin", person.getUserName());
        Assert.assertEquals("Administrator", person.getFirstName());
        Assert.assertEquals("", person.getLastName());
    }

}
