package eu.xenit.apix.tests.translations;

import eu.xenit.apix.alfresco.translation.TranslationService;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.service.ServiceRegistry;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TranslationsServiceUnitTest {

    private static String facetBucketMonthLabel = "faceted-search.date.one-month.label";
    private ServiceRegistry serviceRegistryMock;
    private MessageService messageServiceMock;

    @Before
    public void init() {
        serviceRegistryMock = mock(ServiceRegistry.class);
        when(serviceRegistryMock.getNamespaceService()).thenReturn(null);
        when(serviceRegistryMock.getDictionaryService()).thenReturn(null);

        messageServiceMock = mock(MessageService.class);
        when(messageServiceMock.getMessage(facetBucketMonthLabel)).thenReturn(facetBucketMonthLabel);
    }

    @Test
    public void TestGetTranslatedMessage_GetMessageFromMessageService() {
        TranslationService ts = new TranslationService(serviceRegistryMock, null, null, messageServiceMock);
        Assert.assertEquals(facetBucketMonthLabel, ts.getMessageTranslation(facetBucketMonthLabel));
        Assert.assertNotNull(ts.getMessageTranslation(facetBucketMonthLabel));
    }

    @Test
    public void TestGetTranslatedMessage_NullMessageService() {
        TranslationService ts = new TranslationService(serviceRegistryMock, null, null, null);
        Assert.assertEquals(facetBucketMonthLabel, ts.getMessageTranslation(facetBucketMonthLabel));
        Assert.assertNotNull(ts.getMessageTranslation(facetBucketMonthLabel));
    }

    @Test
    public void TestGetTranslatedMessage_NullParameter() {
        TranslationService ts = new TranslationService(serviceRegistryMock, null, null, messageServiceMock);
        Assert.assertNull(ts.getMessageTranslation(null));
    }
}
