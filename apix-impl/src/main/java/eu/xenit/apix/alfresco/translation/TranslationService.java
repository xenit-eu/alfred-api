package eu.xenit.apix.alfresco.translation;

import com.github.dynamicextensionsalfresco.osgi.OsgiService;
import eu.xenit.apix.alfresco.ApixToAlfrescoConversion;
import eu.xenit.apix.dictionary.IDictionaryService;
import eu.xenit.apix.translation.ITranslationService;
import eu.xenit.apix.translation.PropertyTranslationValue;
import eu.xenit.apix.translation.TranslationValue;
import eu.xenit.apix.translation.Translations;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.CRC32;
import org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint;
import org.alfresco.repo.dictionary.constraint.RegisteredConstraint;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.Constraint;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.i18n.MessageLookup;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Component;


@Component("eu.xenit.apix.translation.ITranslationService")
@OsgiService
public class TranslationService implements ITranslationService {

    private static final Logger logger = LoggerFactory.getLogger(TranslationService.class);

    @Autowired
    private NamespaceService namespaceService;

    @Autowired
    private DictionaryService alfDictionaryService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private IDictionaryService apixDictionaryservice;

    @Autowired
    private ApixToAlfrescoConversion apixToAlfrescoConversion;

    public TranslationService() {
    }

    public TranslationService(ServiceRegistry serviceRegistry, ApixToAlfrescoConversion apixToAlfrescoConversion,
            IDictionaryService dictionaryService, MessageService messageService) {
        this.namespaceService = serviceRegistry.getNamespaceService();
        this.alfDictionaryService = serviceRegistry.getDictionaryService();
        this.apixToAlfrescoConversion = apixToAlfrescoConversion;
        this.apixDictionaryservice = dictionaryService;
        this.messageService = messageService;
    }


    @Override
    public long getTranslationsCheckSum(Locale locale) {
        try (ByteArrayOutputStream bytesOut = new ByteArrayOutputStream()) {
            Map<String, String> translations = I18NUtil.getAllMessages(locale);
            final CRC32 crc = new CRC32();

            ObjectOutputStream oos = new ObjectOutputStream(bytesOut);

            Long modelChecksum = apixDictionaryservice.getContentModelCheckSum();
            oos.writeBytes(modelChecksum.toString());

            //TODO: this is not deterministic and might cause problems later
            for (Map.Entry<String, String> entry : translations.entrySet()) {
                oos.writeBytes(entry.getKey());
                oos.writeBytes(entry.getValue());
            }

            crc.update(bytesOut.toByteArray());

            return crc.getValue();
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public Translations getTranslations(Locale locale) {
        Map<String, String> alfTranslations = I18NUtil.getAllMessages(locale);

        Map<QName, TranslationValue> types = new HashMap<>();
        Map<QName, TranslationValue> aspects = new HashMap<>();
        Map<QName, PropertyTranslationValue> properties = new HashMap<>();
        Map<QName, TranslationValue> associations = new HashMap<>();

//            Map<QName, Map<String, String>> constraints = new HashMap<>();

        for (Map.Entry<String, String> entry : alfTranslations.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            try {
                ResourceBundleTranslationKey translationKey = ResourceBundleTranslationKey
                        .CreateTranslationKey(key, value, namespaceService);

                if (translationKey.getFeature().equals(ResourceBundleTranslationKey.Feature.TYPE)) { // TYPES
                    addTranslationValue(translationKey, types);
                } else if (translationKey.getFeature()
                        .equals(ResourceBundleTranslationKey.Feature.PROPERTY)) { // Properties
                    addPropertyTranslationValue(translationKey, properties);
                } else if (translationKey.getFeature().equals(ResourceBundleTranslationKey.Feature.ASPECT)) { // Aspect
                    addTranslationValue(translationKey, aspects);
                } else if (translationKey.getFeature().equals(ResourceBundleTranslationKey.Feature.ASSOCIATION)) {
                    addTranslationValue(translationKey, associations);
                } else if (translationKey.getFeature().equals(ResourceBundleTranslationKey.Feature.CONSTRAINT)) {
                    // we dont use this anymore
                    //addConstraintValue(translationKey.getQname(), translationKey.getFeatureValue(), translationKey.getValue(), constraints);
                } else if (translationKey.getFeature().equals(ResourceBundleTranslationKey.Feature.MODEL)) {
                    // do nothing?
                } else if (translationKey.getFeature().equals(ResourceBundleTranslationKey.Feature.UNKNOWN)) {
                    // do nothing?
                }
            } catch (NullPointerException npe) {
                logger.debug("Nullpointer", npe);
                logger.debug(key + "=" + value);
            } catch (Exception e) {
                logger.debug("Exception: " + e.getMessage());
                logger.debug(key + "=" + value);
            }

        }

        messageService.setLocale(locale);

        addListConstraintsToProperties(messageService, properties);

        Translations translations = new Translations();
        translations.setTypes(toValueList(types));
        translations.setAspects(toValueList(aspects));
        translations.setProperties(toPropertyValueList(properties));
        translations.setAssociation(toValueList(associations));

        return translations;


    }

    @Override
    public String getMessageTranslation(String messageId) {
        return this.getMessageTranslation(messageId, I18NUtil.getLocale());
    }

    @Override
    public String getMessageTranslation(String messageId, Locale locale) {
        if (this.messageService == null) {
            return messageId;
        }
        String translatedMessage = this.messageService.getMessage(messageId, locale);
        return translatedMessage == null ? messageId : translatedMessage;
    }

    private void addTranslationValue(ResourceBundleTranslationKey translationKey, Map<QName, TranslationValue> map) {
        TranslationValue translationValue = getTranslationValueObject(translationKey.getQname(), map);

        if (translationKey.getFeatureValueType().equals(ResourceBundleTranslationKey.FeatureValueType.TITLE)) {
            translationValue.setTitle(translationKey.getValue());
        } else if (translationKey.getFeatureValueType()
                .equals(ResourceBundleTranslationKey.FeatureValueType.DESCRIPTION)) {
            translationValue.setDescription(translationKey.getValue());
        }

    }

    private void addPropertyTranslationValue(ResourceBundleTranslationKey translationKey,
            Map<QName, PropertyTranslationValue> map) {
        PropertyTranslationValue translationValue = getPropertyTranslationValueObject(translationKey.getQname(), map);

        if (translationKey.getFeatureValueType().equals(ResourceBundleTranslationKey.FeatureValueType.TITLE)) {
            translationValue.setTitle(translationKey.getValue());
        } else if (translationKey.getFeatureValueType()
                .equals(ResourceBundleTranslationKey.FeatureValueType.DESCRIPTION)) {
            translationValue.setDescription(translationKey.getValue());
        }
    }

    private void addConstraintValue(QName qname, String key, String value, Map<QName, Map<String, String>> map) {
        Map<String, String> constraintValues = null;

        if (key.equals(value)) {
            return;
        }

        if (map.containsKey(qname)) {
            constraintValues = map.get(qname);
        } else {
            constraintValues = new HashMap<>();
            map.put(qname, constraintValues);
        }

        constraintValues.put(key, value);
    }

    private TranslationValue getTranslationValueObject(QName qname, Map<QName, TranslationValue> map) {
        TranslationValue translationValue = null;
        if (!map.containsKey(qname)) {
            translationValue = new TranslationValue();
            translationValue.setQname(apixToAlfrescoConversion.apix(qname));
            map.put(qname, translationValue);
        } else {
            translationValue = map.get(qname);
        }

        return translationValue;

    }

    private PropertyTranslationValue getPropertyTranslationValueObject(QName qname,
            Map<QName, PropertyTranslationValue> map) {
        PropertyTranslationValue translationValue = null;
        if (!map.containsKey(qname)) {
            translationValue = new PropertyTranslationValue();
            translationValue.setQname(apixToAlfrescoConversion.apix(qname));
            map.put(qname, translationValue);
        } else {
            translationValue = map.get(qname);
        }

        return translationValue;

    }

    private List<TranslationValue> toValueList(Map<QName, TranslationValue> map) {
        return new ArrayList<TranslationValue>(map.values());
    }

    private List<PropertyTranslationValue> toPropertyValueList(Map<QName, PropertyTranslationValue> map) {
        return new ArrayList<PropertyTranslationValue>(map.values());
    }


    private void addListConstraintsToProperties(MessageLookup messageLookup, Map<QName, PropertyTranslationValue> map) {
        Collection<QName> properties = alfDictionaryService.getAllProperties(null);

        for (QName property : properties) {
            PropertyDefinition propertyDefinition = alfDictionaryService.getProperty(property);

            List<ConstraintDefinition> constraintDefinitions = propertyDefinition.getConstraints();
            if (constraintDefinitions.isEmpty()) {
                continue;
            }

            int listconstraintCounter = 0;
            for (ConstraintDefinition definition : constraintDefinitions) {
                if (definition.getConstraint().getType().equals(ListOfValuesConstraint.CONSTRAINT_TYPE)) {
                    listconstraintCounter++;
                }
            }
            if (listconstraintCounter > 1) {
                logger.warn("Unsupported operation detected! Multiple listconstraints present for property: "
                        + property.toString());
                continue;
            }

            Map<String, String> constraintTranslations = new HashMap<>();

            for (ConstraintDefinition definition : constraintDefinitions) {
                Constraint constraint = definition.getConstraint();

                if (!constraint.getType().equals(ListOfValuesConstraint.CONSTRAINT_TYPE)) {
                    // not a list constraint!
                    continue;
                }

                ListOfValuesConstraint listConstraint = null;
                if (constraint instanceof ListOfValuesConstraint) {
                    listConstraint = (ListOfValuesConstraint) constraint;
                } else if (constraint instanceof RegisteredConstraint) {
                    listConstraint = (ListOfValuesConstraint) ((RegisteredConstraint) constraint)
                            .getRegisteredConstraint();
                } else {
                    continue;
                }

                List<String> allowedValues = listConstraint.getAllowedValues();

                for (String value : allowedValues) {
                    String translation = listConstraint.getDisplayLabel(value, messageLookup);

                    if (!value.equals(translation)) {
                        constraintTranslations.put(value, translation);
                    }
                }

                // there should be only one list constraint here
                break;
            }

            if (constraintTranslations.isEmpty()) {
                continue;
            }

            PropertyTranslationValue translationValue = getPropertyTranslationValueObject(property, map);

            translationValue.setValues(constraintTranslations);

        }
    }
}

