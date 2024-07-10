package eu.xenit.apix.tests.translation;

import eu.xenit.apix.data.QName;
import eu.xenit.apix.tests.JavaApiBaseTest;
import eu.xenit.apix.translation.ITranslationService;
import eu.xenit.apix.translation.PropertyTranslationValue;
import eu.xenit.apix.translation.TranslationValue;
import eu.xenit.apix.translation.Translations;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Stan on 25-Mar-16.
 */
public class TranslationServiceTestJavaApi extends JavaApiBaseTest {

    private final static Logger logger = LoggerFactory.getLogger(TranslationServiceTestJavaApi.class);

    private static String facet_bucket_month_label = "faceted-search.date.one-month.label";
    private ITranslationService service;

    public TranslationServiceTestJavaApi(){
        service = testApplicationContext.getBean(ITranslationService.class);
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
        Assert.assertNotNull(service.getTranslations(Locale.ENGLISH));
        Assert.assertTrue(service.getTranslationsCheckSum(Locale.ENGLISH) != 0); // Highly likely that 0 is error
    }

    @Test
    public void TestGetTranslations() {
        Long serviceStart = System.currentTimeMillis();
        Translations translations = service.getTranslations(Locale.ENGLISH);
        Long serviceStop = System.currentTimeMillis();

        logger.debug("TranslationsService took  " + (serviceStop - serviceStart) + "ms.");

        Assert.assertNotNull(translations);

        //test translations:
        TranslationValue testType1 = new TranslationValue(
                new QName("{http://www.alfresco.org/model/datalist/1.0}dataList"), "Data List folder type",
                "Holds Data List items of the type specified in the dl:dataListItemType property.");
        TranslationValue testType2 = new TranslationValue(
                new QName("{http://www.alfresco.org/model/transfer/1.0}transferLock"), "Transfer Lock",
                "Node type used to represent the transfer lock node");
        TranslationValue testType3 = new TranslationValue(
                new QName("{http://www.alfresco.org/model/content/1.0}category_root"), "Category Root",
                "Root Category");

        boolean testType1found = false;
        boolean testType2found = false;
        boolean testType3found = false;

        List<TranslationValue> types = translations.getTypes();
        for (TranslationValue type : types) {
            if (type.equals(testType1)) {
                testType1found = true;
            } else if (type.equals(testType2)) {
                testType2found = true;
            } else if (type.equals(testType3)) {
                testType3found = true;
            }

            if (testType1found && testType2found && testType3found) {
                break;
            }
        }

        Assert.assertTrue("testType1 is not found: " + testType1.toString(), testType1found);
        Assert.assertTrue("testType2 is not found: " + testType2.toString(), testType2found);
        Assert.assertTrue("testType3 is not found: " + testType3.toString(), testType3found);

        TranslationValue testAspect1 = new TranslationValue(
                new QName("{http://www.alfresco.org/model/system/1.0}archived"), "Archived", "Archived Node");
        TranslationValue testAspect2 = new TranslationValue(
                new QName("{http://www.alfresco.org/model/content/1.0}versionable"), "Versionable", "Versionable");
        TranslationValue testAspect3 = new TranslationValue(
                new QName("{http://www.alfresco.org/model/content/1.0}lockable"), "Lockable", "Lockable");

        boolean testAspect1found = false;
        boolean testAspect2found = false;
        boolean testAspect3found = false;

        List<TranslationValue> aspects = translations.getAspects();
        for (TranslationValue aspect : aspects) {
            if (aspect.equals(testAspect1)) {
                testAspect1found = true;
            } else if (aspect.equals(testAspect2)) {
                testAspect2found = true;
            } else if (aspect.equals(testAspect3)) {
                testAspect3found = true;
            }

            if (testAspect1found && testAspect2found && testAspect3found) {
                break;
            }
        }

        Assert.assertTrue("testAspect1 is not found: " + testAspect1.toString(), testAspect1found);
        Assert.assertTrue("testAspect2 is not found: " + testAspect2.toString(), testAspect2found);
        Assert.assertTrue("testAspect3 is not found: " + testAspect3.toString(), testAspect3found);

        TranslationValue testAssoc1 = new TranslationValue(
                new QName("{http://www.alfresco.org/model/forum/1.0}discussion"), "Discussion",
                "The forum holding the discussion on the object the aspect is applied to");
        TranslationValue testAssoc2 = new TranslationValue(
                new QName("{http://www.alfresco.org/model/content/1.0}avatar"), "Avatar", "The person's avatar image");
        TranslationValue testAssoc3 = new TranslationValue(
                new QName("{http://www.alfresco.org/model/content/1.0}categories"), "Categories",
                "Categories within Category Root");

        boolean testAssoc1found = false;
        boolean testAssoc2found = false;
        boolean testAssoc3found = false;

        List<TranslationValue> assocs = translations.getAssociation();
        for (TranslationValue assoc : assocs) {
            if (assoc.equals(testAssoc1)) {
                testAssoc1found = true;
            } else if (assoc.equals(testAssoc2)) {
                testAssoc2found = true;
            } else if (assoc.equals(testAssoc3)) {
                testAssoc3found = true;
            }

            if (testAssoc1found && testAssoc2found && testAssoc3found) {
                break;
            }
        }

        Assert.assertTrue("testAssoc1 is not found: " + testAssoc1.toString(), testAssoc1found);
        Assert.assertTrue("testAssoc2 is not found: " + testAssoc2.toString(), testAssoc2found);
        Assert.assertTrue("testAssoc3 is not found: " + testAssoc3.toString(), testAssoc3found);

        PropertyTranslationValue testProp2 = new PropertyTranslationValue(
                new QName("{http://www.alfresco.org/model/workflow/invite/moderated/1.0}reviewOutcome"), null, null,
                new HashMap<String, String>() {{
                    put("approve", "Approve");
                    put("reject", "Reject");
                }});

        boolean testProp2found = false;

        List<PropertyTranslationValue> properties = translations.getProperties();
        for (TranslationValue prop : properties) {
            System.out.println(prop.toString());
            if (prop.equals(testProp2)) {
                testProp2found = true;
            }

            if (testProp2found) {
                break;
            }
        }

        Assert.assertTrue("testProp2 is not found: " + testProp2.toString(), testProp2found);


    }

    @Test
    public void TestGetTranslatedMessage() {
        Assert.assertNotNull(service.getMessageTranslation(facet_bucket_month_label));
    }

    @Test
    public void TestGetTranslatedMessage_LocaleEN() {
        String translatedMessage = service.getMessageTranslation(facet_bucket_month_label, Locale.ENGLISH);
        Assert.assertNotNull(translatedMessage);
        Assert.assertEquals("This month", translatedMessage);
    }

    @Test
    public void TestGetTranslatedMessage_LocaleFR() {
        String translatedMessage = service.getMessageTranslation(facet_bucket_month_label, Locale.FRENCH);
        Assert.assertNotNull(translatedMessage);
        Assert.assertEquals(translatedMessage, "Ce mois");
    }

    @Test
    public void TestGetTranslatedMessage_LocaleFR_NEQ_LocaleEN() {
        String translatedMessage = service.getMessageTranslation(facet_bucket_month_label, Locale.FRENCH);
        Assert.assertNotNull(translatedMessage);
        Assert.assertNotEquals(translatedMessage, "This month");
    }

    //    @Test
    public void benchMark() {
        Long[] duration_eng = new Long[10000];
        Long[] duration_nl = new Long[duration_eng.length];

        for (int i = 0; i < duration_eng.length; i++) {
            Long start = System.currentTimeMillis();
            Translations translations = service.getTranslations(Locale.ENGLISH);
            Long stop = System.currentTimeMillis();

            duration_eng[i] = stop - start;
        }

        logger.debug("Run eng took on average " + average(duration_eng) + "ms.");

        for (int i = 0; i < duration_nl.length; i++) {
            Long start = System.currentTimeMillis();
            Translations translations = service.getTranslations(Locale.forLanguageTag("nl"));
            Long stop = System.currentTimeMillis();

            duration_nl[i] = stop - start;
        }

        logger.debug("Run nl took " + average(duration_nl) + "ms.");
    }

    private double average(Long[] array) {
        Long sum = 0l;

        for (Long value : array) {
            sum += value;
        }

        double result = (double) sum / array.length;

        return result;
    }


}
