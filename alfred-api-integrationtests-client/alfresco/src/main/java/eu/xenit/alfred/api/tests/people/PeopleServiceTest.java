package eu.xenit.alfred.api.tests.people;

import static org.junit.Assert.assertEquals;

import eu.xenit.alfred.api.people.IPeopleService;
import eu.xenit.alfred.api.people.Person;
import eu.xenit.alfred.api.tests.JavaApiBaseTest;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Jasperhilven on 24-Oct-16.
 */
public class PeopleServiceTest extends JavaApiBaseTest {

    private final static Logger logger = LoggerFactory.getLogger(PeopleServiceTest.class);

    private final IPeopleService peopleService;
    private final PersonService alfrescoPersonService;

    public PeopleServiceTest() {
        // initialise the local beans
        peopleService = getBean(IPeopleService.class);
        alfrescoPersonService = serviceRegistry.getPersonService();
    }

    @Before
    public void Setup() {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
    }

    @Test
    public void TestGetPerson() {
        final HashMap<QName, Serializable> props = new HashMap<QName, Serializable>();

        final String userName = "UserNameServiceTest";
        final String email = "Email";
        final String firstName = "FirstName";
        final String lastName = "LastName";
        final PeopleServiceTest self = this;
        props.put(ContentModel.PROP_EMAIL, email);
        props.put(ContentModel.PROP_FIRSTNAME, firstName);
        props.put(ContentModel.PROP_LASTNAME, lastName);
        props.put(ContentModel.PROP_USERNAME, userName);

        // custom type containing the mandatory property but this property is missing.

        serviceRegistry.getRetryingTransactionHelper()
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>() {
                    @Override
                    public Void execute() throws Throwable {

                        self.cleanUp();
                        //Create person
                        alfrescoPersonService.deletePerson(userName);

                        return null;
                    }
                }, false, true);

        serviceRegistry.getRetryingTransactionHelper()
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>() {
                    @Override
                    public Void execute() throws Throwable {

                        NodeRef person = alfrescoPersonService.createPerson(props);
                        Person p = peopleService.GetPerson(c.alfredApi(person));
                        assertEquals(p.getEmailAddress(), email);
                        assertEquals(p.getFirstName(), firstName);
                        assertEquals(p.getLastName(), lastName);
                        assertEquals(p.getUserName(), userName);
                        assert (p.getGroups() != null);
                        return null;
                    }
                }, false, true);

    }

    @Test(expected = NoSuchElementException.class)
    public void TestGetNonExistentPerson() {
        serviceRegistry.getRetryingTransactionHelper()
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>() {
                    @Override
                    public Void execute() throws Throwable {

                        Person p = peopleService.GetPerson("NonExistentUser");
                        //Will return NoSuchElementException for non-existing users
                        return null;
                    }
                }, false, true);
    }

    @Test
    public void TestGetPeople() {
        final HashMap<QName, Serializable> propsA = new HashMap<QName, Serializable>();
        final HashMap<QName, Serializable> propsB = new HashMap<QName, Serializable>();

        final String userNameA = "UserNameA";
        final String emailA = "EmailA";
        final String firstNameA = "FirstNameA";
        final String lastNameA = "LastNameA";
        final String userNameB = "UserNameB";
        final String emailB = "EmailB";
        final String firstNameB = "FirstNameB";
        final String lastNameB = "LastNameB";
        final PeopleServiceTest self = this;

        propsA.put(ContentModel.PROP_EMAIL, emailA);
        propsA.put(ContentModel.PROP_FIRSTNAME, firstNameA);
        propsA.put(ContentModel.PROP_LASTNAME, lastNameA);
        propsA.put(ContentModel.PROP_USERNAME, userNameA);

        propsB.put(ContentModel.PROP_EMAIL, emailB);
        propsB.put(ContentModel.PROP_FIRSTNAME, firstNameB);
        propsB.put(ContentModel.PROP_LASTNAME, lastNameB);
        propsB.put(ContentModel.PROP_USERNAME, userNameB);

        // custom type containing the mandatory property but this property is missing.

        serviceRegistry.getRetryingTransactionHelper()
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>() {
                    @Override
                    public Void execute() throws Throwable {

                        self.cleanUp();
                        alfrescoPersonService.deletePerson(userNameA);
                        alfrescoPersonService.deletePerson(userNameB);

                        return null;
                    }
                }, false, true);

        serviceRegistry.getRetryingTransactionHelper()
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>() {
                    @Override
                    public Void execute() throws Throwable {
                        NodeRef personA = alfrescoPersonService.createPerson(propsA);
                        NodeRef personB = alfrescoPersonService.createPerson(propsB);
                        List<Person> p = peopleService.GetPeople();
                        assert (p.size() > 0);
                        assert (p.contains(personA));
                        assert (p.contains(personB));
                        return null;
                    }
                }, false, true);
    }
}
