package eu.xenit.alfred.api.tests.properties;


import eu.xenit.alfred.api.data.QName;
import eu.xenit.alfred.api.properties.IPropertyService;
import eu.xenit.alfred.api.properties.PropertyDefinition;
import eu.xenit.alfred.api.tests.JavaApiBaseTest;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Jasperhilven on 24-Jan-17.
 */
public class PropertyServiceTest extends JavaApiBaseTest {

    private final static Logger logger = LoggerFactory.getLogger(PropertyServiceTest.class);

    private final IPropertyService propertyService;

    public PropertyServiceTest() {
        // initialise the local beans
        propertyService = getBean(IPropertyService.class);
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
