package eu.xenit.alfred.api.rest.v2.tests;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.xenit.alfred.api.people.Person;
import eu.xenit.alfred.api.rest.RestCall;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Request;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PeopleTest extends RestV2BaseTest {

    // A dotted username: the part after the dot must not be mistaken for a format extension.
    private static final String DOTTED_USER = "people.test.user";

    @Before
    public void setup() {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        transactionHelper.doInTransaction(() -> createUser(DOTTED_USER, DOTTED_USER, "People",
                "people.test@example.com"), false, true);
    }

    @Test
    public void testGetSelf() throws IOException {
        final String userName = "-me-";
        String url = createAlfredApiUrl("/people/%s", userName);
        HttpResponse httpResponse = Request.Get(url).execute().returnResponse();
        String result = EntityUtils.toString(httpResponse.getEntity());
        Person person = new ObjectMapper().readValue(result, Person.class);
        Assert.assertEquals("admin@alfresco.com", person.getEmailAddress());
        Assert.assertEquals("admin", person.getUserName());
        Assert.assertEquals("Administrator", person.getFirstName());
        Assert.assertEquals("", person.getLastName());
    }

    @Test
    public void testListPeople() throws IOException {
        JsonNode people = RestCall.assertJson(RestCall.get(createAlfredApiUrl("/people")));
        List<String> userNames = new ArrayList<>();
        people.forEach(person -> userNames.add(person.get("userName").asText()));
        Assert.assertTrue(userNames.contains("admin"));
        Assert.assertTrue(userNames.contains(DOTTED_USER));
    }

    @Test
    public void testMeIsTheCurrentUser() throws IOException {
        JsonNode me = RestCall.assertJson(RestCall.get(createAlfredApiUrl("/people/-me-")));
        JsonNode admin = RestCall.assertJson(RestCall.get(createAlfredApiUrl("/people/admin")));
        Assert.assertEquals(admin, me);
    }

    @Test
    public void testGetPersonByNameIncludingDottedNames() throws IOException {
        JsonNode person = RestCall.assertJson(RestCall.get(createAlfredApiUrl("/people/%s", DOTTED_USER)));
        Assert.assertEquals(DOTTED_USER, person.get("userName").asText());

        RestCall.assertText(RestCall.get(createAlfredApiUrl("/people/%s", "nobody.unknown")),
                HttpStatus.SC_NOT_FOUND, "User nobody.unknown does not exist");
    }

    @Test
    public void testGetPersonById() throws IOException {
        JsonNode admin = RestCall.assertJson(RestCall.get(createAlfredApiUrl("/people/admin")));
        String[] ref = admin.get("nodeRef").asText().split("://|/");

        JsonNode byId = RestCall.assertJson(
                RestCall.get(createAlfredApiUrl("/people/id/%s/%s/%s", ref[0], ref[1], ref[2])));
        Assert.assertEquals(admin, byId);

        String guid = "00000000-0000-0000-0000-000000000000";
        RestCall.assertText(RestCall.get(createAlfredApiUrl("/people/id/workspace/SpacesStore/%s", guid)),
                HttpStatus.SC_NOT_FOUND, "User with NodeRef=workspace://SpacesStore/" + guid + " does not exist");
    }

    @Test
    public void testContainerGroupsOnTheDocumentedUrl() throws IOException {
        // The documented URL. The MVC mapping had an accidental space, so it returned 404 before the migration.
        JsonNode groups = RestCall.assertJson(RestCall.get(createAlfredApiUrl("/people/containergroups/admin")));
        List<String> names = new ArrayList<>();
        groups.forEach(group -> names.add(group.asText()));
        Assert.assertTrue(names.toString(), names.contains("GROUP_ALFRESCO_ADMINISTRATORS"));
    }

    @Test
    public void testSpecificRoutesWithoutTheirSuffixFallBackToTheNameRoute() throws IOException {
        // "id" and "containergroups" on their own are treated as usernames, as before the migration.
        RestCall.assertText(RestCall.get(createAlfredApiUrl("/people/id")),
                HttpStatus.SC_NOT_FOUND, "User id does not exist");
        RestCall.assertText(RestCall.get(createAlfredApiUrl("/people/containergroups")),
                HttpStatus.SC_NOT_FOUND, "User containergroups does not exist");
    }

    @Test
    public void testPlainTextErrorsAreUtf8() throws IOException {
        String name = "Україна";
        String url = makeAlfrescoBaseurlAdmin() + "/apix/v2/people/" + URLEncoder.encode(name, StandardCharsets.UTF_8);
        RestCall.assertUtf8Text(RestCall.get(url), HttpStatus.SC_NOT_FOUND, "User " + name + " does not exist");
    }

    @Test
    public void testAnonymousIsUnauthorized() throws IOException {
        Assert.assertEquals(HttpStatus.SC_UNAUTHORIZED, RestCall.get(makeAlfrescoBaseurl() + "/apix/v2/people").status());
    }
}
