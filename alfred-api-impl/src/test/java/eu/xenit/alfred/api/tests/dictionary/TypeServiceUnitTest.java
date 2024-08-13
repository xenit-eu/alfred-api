package eu.xenit.alfred.api.tests.dictionary;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import eu.xenit.alfred.api.alfresco.AlfredApiToAlfrescoConversion;
import eu.xenit.alfred.api.alfresco.dictionary.TypeService;
import eu.xenit.alfred.api.data.QName;
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
        AlfredApiToAlfrescoConversion alfredApiToAlfrescoConversionMock = mock(AlfredApiToAlfrescoConversion.class);
        when(alfredApiToAlfrescoConversionMock.alfresco(invalidQname)).thenThrow(NamespaceException.class);
        TypeService typeService = new TypeService(dictionaryServiceMock, alfredApiToAlfrescoConversionMock);
        Assertions.assertNull(typeService.GetTypeDefinition(invalidQname), "[FAIL] An invalid qname did not result in a null response.");
    }

}
