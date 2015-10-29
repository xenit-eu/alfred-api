package eu.xenit.apix.tests.properties;

import eu.xenit.apix.alfresco.ApixToAlfrescoConversion;
import eu.xenit.apix.tests.BaseTest;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by Jasperhilven on 12-Jan-17.
 */
public class PropertyServiceTest50 extends BaseTest {

    private final static Logger logger = LoggerFactory.getLogger(PropertyServiceTest50.class);
    @Autowired
    private ApixToAlfrescoConversion c;
    @Autowired
    private DictionaryService d;

    @Test
    public void TestGetPropertyName() {
        //PropertyServiceTest50 underTest = new PropertyServiceImpl50(this.d,this.c);
        //PropertyDefinition definition = underTest.GetPropertyDefinition(c.apix(ContentModel.PROP_NAME));
        //Assert.assertTrue("Definition should return a name", definition.getName().getValue() == "name");
    }
}
