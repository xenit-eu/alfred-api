package eu.xenit.apix.tests.dictionary;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import eu.xenit.apix.alfresco.ApixToAlfrescoConversion;
import eu.xenit.apix.alfresco.dictionary.TypeService;
import eu.xenit.apix.data.QName;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.NamespaceException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TypeServiceUnitTest {

    private static final Logger log = LoggerFactory.getLogger(TypeServiceUnitTest.class);

    @Test
    public void testGetTypeDefinition_returnsNull_whenQnameInvalid() {
        QName invalidQname = new QName("someNamespace:someQname");
        DictionaryService dictionaryServiceMock = mock(DictionaryService.class);
        ApixToAlfrescoConversion apixToAlfrescoConversionMock = mock(ApixToAlfrescoConversion.class);
        when(apixToAlfrescoConversionMock.alfresco(invalidQname)).thenThrow(NamespaceException.class);
        TypeService typeService = new TypeService(dictionaryServiceMock, apixToAlfrescoConversionMock);
        Assertions.assertNull(typeService.GetTypeDefinition(invalidQname), "[FAIL] An invalid qname did not result in a null response.");
    }

}
