package eu.xenit.apix.alfresco.translation;

import org.alfresco.service.namespace.InvalidQNameException;
import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Stan on 30-Mar-16.
 */
public class ResourceBundleTranslationKey {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceBundleTranslationKey.class);
    private Feature feature;
    private QName model;
    private QName qname;
    private FeatureValueType featureValueType;
    private String featureValue;
    private String value;
    public ResourceBundleTranslationKey() {
    }

    public static ResourceBundleTranslationKey CreateTranslationKey(String key, String value,
            NamespaceService namespaceService) {
        // <model_prefix>_<model_name>.[title|description]
        // <model_prefix>_<model_name>.<feature>.<feature_prefix>_<feature_name>. [title|description]
        // listconstraint.<constraint_prefix>_<constraint_name>.<allowed_value>

        ResourceBundleTranslationKey translationKey = new ResourceBundleTranslationKey();
        translationKey.setValue(value);

        if (key.startsWith("listconstraint")) {
            translationKey.setFeature(Feature.CONSTRAINT);
            translationKey.setFeatureValueType(FeatureValueType.VALUE);
            String[] parts = splitConstraintKey(key);

            QName qname = createQName(parts[1], parts[2], namespaceService);
            if (qname == null) {
                return createErrorValue(key, value);
            }

            translationKey.setQname(qname);

            translationKey.setFeatureValue(parts[3]);

        } else {
            String[] parts = splitTranslationKey(key);

            if (parts == null) {
                return createErrorValue(key, value);
            } else if (parts.length == 3) {
                translationKey.setFeature(Feature.MODEL);
                QName model = createQName(parts[0], parts[1], namespaceService);
                if (model == null) {
                    return createErrorValue(key, value);
                }

                translationKey.setModel(model);

                if (parts[2].equals("title")) {
                    translationKey.setFeatureValueType(FeatureValueType.TITLE);
                } else if (parts[2].equals("description")) {
                    translationKey.setFeatureValueType(FeatureValueType.DESCRIPTION);
                } else {
                    translationKey.setFeatureValueType(FeatureValueType.UNKNOWN);
                }

                translationKey.setFeatureValue(parts[2]);

            } else if (parts.length == 6) {
                QName model = createQName(parts[0], parts[1], namespaceService);
                if (model == null) {
                    return createErrorValue(key, value);
                }

                translationKey.setModel(model);

                if (parts[2].equals("type")) {
                    translationKey.setFeature(Feature.TYPE);
                } else if (parts[2].equals("aspect")) {
                    translationKey.setFeature(Feature.ASPECT);
                } else if (parts[2].equals("property")) {
                    translationKey.setFeature(Feature.PROPERTY);
                } else if (parts[2].equals("association")) {
                    translationKey.setFeature(Feature.ASSOCIATION);
                } else {
                    translationKey.setFeature(Feature.UNKNOWN);
                }

                QName qname = createQName(parts[3], parts[4], namespaceService);
                if (qname == null) {
                    return createErrorValue(key, value);
                }

                translationKey.setQname(qname);

                if (parts[5].equals("title")) {
                    translationKey.setFeatureValueType(FeatureValueType.TITLE);
                } else if (parts[5].equals("description")) {
                    translationKey.setFeatureValueType(FeatureValueType.DESCRIPTION);
                } else {
                    translationKey.setFeatureValueType(FeatureValueType.UNKNOWN);
                }

                translationKey.setFeatureValue(parts[5]);
            }


        }

        return translationKey;
    }

    private static QName createQName(String prefix, String localName, NamespaceService namespaceService) {
        String shortQname = prefix + ":" + localName;

        QName result = null;
        try {
            result = QName.createQName(shortQname, namespaceService);
        } catch (InvalidQNameException iqe) {
            LOGGER.debug("Unable to create QName, " + shortQname + " is an invallid qname");
        } catch (NamespaceException ne) {
            LOGGER.debug("Unable to create QName, " + shortQname + " has an invallid namespace");
        }

        return result;

    }

    private static ResourceBundleTranslationKey createErrorValue(String key, String value) {
        ResourceBundleTranslationKey translationKey = new ResourceBundleTranslationKey();
        translationKey.setModel(null);
        translationKey.setFeature(Feature.UNKNOWN);
        translationKey.setQname(null);
        translationKey.setFeatureValueType(FeatureValueType.UNKNOWN);
        translationKey.setFeatureValue(key);
        translationKey.setValue(value);

        return translationKey;
    }

    private static String[] splitTranslationKey(String key) {
        // <model_prefix>_<model_name>.[title|description]
        // <model_prefix>_<model_name>.<feature>.<feature_prefix>_<feature_name>. [title|description]

        String model_prefix;
        String model_name;
        String feature;
        String feature_prefix;
        String feature_name;
        String tail;

        int index1 = key.indexOf('_');
        if (index1 == -1) {
            return null;
        }

        int indexCheck = key.indexOf('.');
        if (indexCheck < index1) {
            return null;
        }

        int index2 = key.indexOf(".", index1);
        if (index2 == -1) {
            return null;
        }

        model_prefix = key.substring(0, index1);
        model_name = key.substring(index1 + 1, index2);

        int index3 = key.indexOf(".", index2 + 1);
        if (index3 == -1) {
            tail = key.substring(index2 + 1);
            if (!(tail.equals("title") || tail.equals("description"))) {
                return null;
            }

            String[] parts = new String[3];
            parts[0] = model_prefix;
            parts[1] = model_name;
            parts[2] = tail;

            return parts;
        }

        int index4 = key.indexOf("_", index3);
        if (index4 == -1) {
            return null;
        }

        int index5 = key.indexOf(".", index4);
        if (index5 == -1) {
            return null;
        }

        feature = key.substring(index2 + 1, index3);
        feature_prefix = key.substring(index3 + 1, index4);
        feature_name = key.substring(index4 + 1, index5);
        tail = key.substring(index5 + 1);

        if (!(tail.equals("title") || tail.equals("description"))) {
            return null;
        }

        String[] parts = new String[6];
        parts[0] = model_prefix;
        parts[1] = model_name;
        parts[2] = feature;
        parts[3] = feature_prefix;
        parts[4] = feature_name;
        parts[5] = tail;

        return parts;
    }

    /**
     * this thing is needed because alfresco likes to put '.' and '_' everywhere. Screw you alfresco!
     */
    private static String[] splitConstraintKey(String key) {
        String[] parts = new String[4];

        // listconstraint.<constraint_prefix>_<constraint_name>.<allowed_value>

        int index1 = key.indexOf(".");
        int index2 = key.indexOf("_", index1);
        int index3 = key.indexOf(".", index2);

        parts[0] = key.substring(0, index1);
        parts[1] = key.substring(index1 + 1, index2);
        parts[2] = key.substring(index2 + 1, index3);
        parts[3] = key.substring(index3 + 1);

        return parts;
    }

    public Feature getFeature() {
        return feature;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }

    public QName getModel() {
        return model;
    }

    public void setModel(QName model) {
        this.model = model;
    }

    public QName getQname() {
        return qname;
    }

    public void setQname(QName qname) {
        this.qname = qname;
    }

    public FeatureValueType getFeatureValueType() {
        return featureValueType;
    }

    public void setFeatureValueType(FeatureValueType featureValueType) {
        this.featureValueType = featureValueType;
    }

    public String getFeatureValue() {
        return featureValue;
    }

    public void setFeatureValue(String featureValue) {
        this.featureValue = featureValue;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public enum Feature {
        MODEL,
        TYPE,
        ASPECT,
        PROPERTY,
        ASSOCIATION,
        CONSTRAINT,
        UNKNOWN
    }

    public enum FeatureValueType {
        TITLE,
        DESCRIPTION,
        VALUE,
        UNKNOWN
    }
}
