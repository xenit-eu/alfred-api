package eu.xenit.apix.tests.properties;


import eu.xenit.apix.alfresco.ApixToAlfrescoConversion;
import eu.xenit.apix.data.QName;
import eu.xenit.apix.permissions.IPermissionService;
import eu.xenit.apix.properties.IPropertyService;
import eu.xenit.apix.properties.PropertyDefinition;
import eu.xenit.apix.server.ApplicationContextProvider;
import eu.xenit.apix.tests.BaseTest;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

/**
 * Created by Jasperhilven on 24-Jan-17.
 */
public class PropertyServiceTest extends BaseTest {

    private final static Logger logger = LoggerFactory.getLogger(PropertyServiceTest.class);
    private ApplicationContext testApplicationContext;
    private ServiceRegistry serviceRegistry;

    private IPropertyService propertyService;
    private ApixToAlfrescoConversion c;
    private MessageService messageService;

    @Before
    public void Setup() {
        // initialiseBeans BaseTest
        initialiseBeans();
        // initialise the local beans
        testApplicationContext = ApplicationContextProvider.getApplicationContext();
        serviceRegistry = (ServiceRegistry) testApplicationContext.getBean(ServiceRegistry.class);
        c =  (ApixToAlfrescoConversion) testApplicationContext.getBean(ApixToAlfrescoConversion.class);
        propertyService = (IPropertyService) testApplicationContext.getBean(IPropertyService.class);
        messageService = serviceRegistry.getMessageService();
    }

    @Test
    public void TestGetNamePropertyLong() {
        QName nameLongRepresentation = new QName("{http://www.alfresco.org/model/content/1.0}name");
        PropertyDefinition def = this.propertyService.GetPropertyDefinition(nameLongRepresentation);
        TestPropertyDefinition(def);
    }

    @Test
    public void TestGetNamePropertyShort() {
        QName nameShortRepresentation = new QName("cm:name");
        PropertyDefinition def = this.propertyService.GetPropertyDefinition(nameShortRepresentation);
        TestPropertyDefinition(def);
    }

    private void TestPropertyDefinition(PropertyDefinition def) {
        Assert.assertNotNull(def);
        Assert.assertNotNull(def.getName());
        Assert.assertNotNull(def.getTitle());
        Assert.assertNotNull(def.getDescription());
        Assert.assertNotNull(def.getDataType());
        Assert.assertEquals(def.getDataType().getValue(), "{http://www.alfresco.org/model/dictionary/1.0}text");
        Assert.assertFalse(def.isMultiValued());
        Assert.assertTrue(def.isMandatory());
        Assert.assertTrue(def.isEnforced());
    }

    @Test
    public void TestPropertyWithInvalidLocalNameShouldReturnNull() {
        QName nameShortRepresentation = new QName("cm:nowayJoseThisPropertyExists");
        PropertyDefinition def = this.propertyService.GetPropertyDefinition(nameShortRepresentation);
        Assert.assertNull(def);
    }

    @Test
    public void TestPropertyWithInvalidNameSpaceShouldReturnNull() {
        QName nameShortRepresentation = new QName("nowayJoseThisnamespaceExists:name");
        PropertyDefinition def = this.propertyService.GetPropertyDefinition(nameShortRepresentation);
        Assert.assertNull(def);
    }

}
