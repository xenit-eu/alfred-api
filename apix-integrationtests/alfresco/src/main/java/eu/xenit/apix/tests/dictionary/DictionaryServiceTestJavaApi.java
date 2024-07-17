package eu.xenit.apix.tests.dictionary;

import eu.xenit.apix.data.QName;
import eu.xenit.apix.dictionary.IDictionaryService;
import eu.xenit.apix.dictionary.aspects.AspectDefinition;
import eu.xenit.apix.tests.JavaApiBaseTest;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * Created by Michiel Huygen on 24/11/2015.
 */
public class DictionaryServiceTestJavaApi extends JavaApiBaseTest {

    private IDictionaryService service;

    public DictionaryServiceTestJavaApi(){
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
