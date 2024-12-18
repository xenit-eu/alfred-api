package eu.xenit.alfred.api.tests.translations;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import eu.xenit.alfred.api.alfresco.translation.TranslationService;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.service.ServiceRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TranslationsServiceUnitTest {

    private static String facetBucketMonthLabel = "faceted-search.date.one-month.label";
    private ServiceRegistry serviceRegistryMock;
    private MessageService messageServiceMock;

    @BeforeEach
    public void init() {
        serviceRegistryMock = mock(ServiceRegistry.class);
        when(serviceRegistryMock.getNamespaceService()).thenReturn(null);
        when(serviceRegistryMock.getDictionaryService()).thenReturn(null);

        messageServiceMock = mock(MessageService.class);
        when(messageServiceMock.getMessage(facetBucketMonthLabel)).thenReturn(facetBucketMonthLabel);
    }

    @Test
    public void TestGetTranslatedMessage_GetMessageFromMessageService() {
        TranslationService ts = new TranslationService(serviceRegistryMock, null, null);
        Assertions.assertEquals(facetBucketMonthLabel, ts.getMessageTranslation(facetBucketMonthLabel));
        Assertions.assertNotNull(ts.getMessageTranslation(facetBucketMonthLabel));
    }

    @Test
    public void TestGetTranslatedMessage_NullMessageService() {
        TranslationService ts = new TranslationService(serviceRegistryMock, null, null);
        Assertions.assertEquals(facetBucketMonthLabel, ts.getMessageTranslation(facetBucketMonthLabel));
        Assertions.assertNotNull(ts.getMessageTranslation(facetBucketMonthLabel));
    }

    @Test
    public void TestGetTranslatedMessage_NullParameter() {
        TranslationService ts = new TranslationService(serviceRegistryMock, null, null);
        Assertions.assertNull(ts.getMessageTranslation(null));
    }
}
