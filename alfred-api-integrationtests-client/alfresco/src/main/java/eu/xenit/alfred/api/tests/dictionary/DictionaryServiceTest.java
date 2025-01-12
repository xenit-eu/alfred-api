package eu.xenit.alfred.api.tests.dictionary;

import eu.xenit.alfred.api.data.QName;
import eu.xenit.alfred.api.dictionary.IDictionaryService;
import eu.xenit.alfred.api.dictionary.aspects.AspectDefinition;
import eu.xenit.alfred.api.tests.JavaApiBaseTest;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * Created by Michiel Huygen on 24/11/2015.
 */
public class DictionaryServiceTest extends JavaApiBaseTest {

    private final IDictionaryService service;

    public DictionaryServiceTest() {
        service = getBean(IDictionaryService.class);
    }

    @Before
    public void Setup() {
        AuthenticationUtil.setFullyAuthenticatedUser("admin");
    }


    /**
     * Operational integration test
     */
    @Test
    public void TestGetChecksum() {
        Assert.assertTrue(service.getContentModelCheckSum() != 0); // Highly likely that 0 is error
    }

    /**
     * Operational integration test
     */
    @Test
    public void TestGetChecksum_NoChange() {
        Assert.assertEquals(service.getContentModelCheckSum(), service.getContentModelCheckSum());
        Assert.assertEquals(service.getContentModelCheckSum(), service.getContentModelCheckSum());
    }

    @Test
    public void TestInvalidQNameNamespaceReturnsNull() {
        AspectDefinition notExisting = service.GetAspectDefinition(new QName("doesnotexist:name"));
        Assert.assertNull(notExisting);
    }

    @Test
    public void TestInvalidQNameLocalNameReturnsNull() {
        AspectDefinition notExisting = service.GetAspectDefinition(new QName("cm:doesnotexist"));
        Assert.assertNull(notExisting);
    }


}
