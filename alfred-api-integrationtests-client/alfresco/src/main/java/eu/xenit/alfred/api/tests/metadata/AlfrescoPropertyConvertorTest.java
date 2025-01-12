package eu.xenit.alfred.api.tests.metadata;

import eu.xenit.alfred.api.alfresco.metadata.AlfrescoPropertyConvertor;
import eu.xenit.alfred.api.tests.JavaApiBaseTest;
import java.util.Arrays;
import java.util.List;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.junit.Assert;
import org.junit.Test;


public class AlfrescoPropertyConvertorTest extends JavaApiBaseTest {

    @Test
    public void testPropertyConvertOnRegisteredConstraint() throws Exception {
        // See bug https://xenitsupport.jira.com/browse/ALFREDAPI-299

        // Bug was always reproducable with this particular property
        String propertyName = "{http://www.alfresco.org/model/content/smartfolder/1.0}system-template-location";
        String propertyValue = "Over darrr";

        AlfrescoPropertyConvertor convertor = new AlfrescoPropertyConvertor(serviceRegistry, c);
        Pair<eu.xenit.alfred.api.data.QName, List<String>> result = convertor.toModelPropertyValue(
                QName.createQName(propertyName),
                propertyValue);

        Assert.assertEquals(new eu.xenit.alfred.api.data.QName(propertyName), result.getFirst());
        Assert.assertEquals(Arrays.asList(propertyValue), result.getSecond());
    }
}
