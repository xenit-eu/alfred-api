package eu.xenit.apix.alfresco.metadata;

import eu.xenit.apix.alfresco.ApixToAlfrescoConversion;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.repository.datatype.TypeConverter;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO8601DateFormat;
import org.alfresco.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component("eu.xenit.apix.alfresco.metadata.AlfrescoPropertyConvertor")
public class AlfrescoPropertyConvertor {

    private final static Logger logger = LoggerFactory.getLogger(AlfrescoPropertyConvertor.class);
    private ApixToAlfrescoConversion c;
    private DictionaryService dictionaryService;


    @Autowired
    public AlfrescoPropertyConvertor(DictionaryService dictionaryService, ApixToAlfrescoConversion c) {
        this.dictionaryService = dictionaryService;
        this.c = c;
    }

    public Map<eu.xenit.apix.data.QName, List<String>> toModelPropertyValueList(
            Map<QName, Serializable> nodeProperties) {
        Map<eu.xenit.apix.data.QName, List<String>> propertyValues = new HashMap<>();
        for (Map.Entry<org.alfresco.service.namespace.QName, Serializable> nodeProp : nodeProperties.entrySet()) {
            try {
                final Pair<eu.xenit.apix.data.QName, List<String>> value = toModelPropertyValue(
                        nodeProp.getKey(), nodeProp.getValue());
                if (value.getSecond() != null) {
                    propertyValues.put(value.getFirst(), value.getSecond());
                } else {
                    propertyValues.put(value.getFirst(), new ArrayList<String>());
                }
            } catch (Exception e) {
                logger.error("Skipping property " + nodeProp.getKey().toString() + " due to error", e);
            }
        }
        return propertyValues;
    }

    public Pair<eu.xenit.apix.data.QName, List<String>> toModelPropertyValue(
            org.alfresco.service.namespace.QName alfQName, Serializable data) throws Exception {
        TypeConverter typeConverter = DefaultTypeConverter.INSTANCE;

        eu.xenit.apix.data.QName propQName = c.apix(alfQName);
        if (data == null) {
            return new Pair<>(propQName, null);
        }

        PropertyDefinition propertyDefinition = dictionaryService.getProperty(alfQName);
        if (propertyDefinition == null) {
            logger.debug("value for residual property " + propQName + " is " + data.toString());
            List<String> valueList = new ArrayList<String>();
            valueList.add(data.toString());
            return new Pair<>(propQName, valueList);
        }

        DataTypeDefinition type = propertyDefinition.getDataType();
        if (type == null) {
            throw new Exception("Data type for " + alfQName.toString() + " not found");
        }

        boolean isDate = false;
        if (type.getName().equals(DataTypeDefinition.DATE) || type.getName().equals(DataTypeDefinition.DATETIME)) {
            isDate = true;
        }

        if (propertyDefinition.isMultiValued()) {
            List<String> multiVal = new ArrayList<String>();

            // convertQuery the serialized data to a collection
            Class typeClass = Class.forName(type.getJavaClassName());
            Collection objects = typeConverter.getCollection(typeClass, data);

            // create a list of strings from the converted collection
            for (Object val : objects) {
                if (val == null) {
                    logger.warn("Found null in multivalued property '" + alfQName.toString() + "'");
                    continue;
                }
                if (isDate) {
                    multiVal.add(ISO8601DateFormat.format((Date) val));
                    continue;
                }
                multiVal.add(val.toString());
            }
            return new Pair<>(propQName, multiVal);
        } else {
            List<String> valuelist = new ArrayList<String>();
            // TODO: reconsider to remove this since we are starting from serializables already. Probably wrap
            Object object = typeConverter.convert(type, data);
            // TODO: hack, should be proper multilanguage support
            if (object instanceof MLText) {
                MLText mlText = (MLText) object;
                object = mlText.getDefaultValue();
            }
            if (isDate) {
                valuelist.add(ISO8601DateFormat.format((Date) object));
            } else {
                valuelist.add(object.toString());
            }
            return new Pair<>(propQName, valuelist);
        }
    }

    public Map<org.alfresco.service.namespace.QName, Serializable> toAlfrescoPropertyMap(
            Map<String, List<String>> propertyValueList) {
        Map<org.alfresco.service.namespace.QName, Serializable> alfProperties = new HashMap<>(propertyValueList.size());
        for (Map.Entry<String, List<String>> entry : propertyValueList.entrySet()) {
            org.alfresco.service.namespace.QName alfQName = QName.createQName(entry.getKey());
            Serializable data = toAlfrescoPropertyValue(new Pair<>(entry.getKey(), entry.getValue()));
            alfProperties.put(alfQName, data);
        }
        return alfProperties;
    }

    public Serializable toAlfrescoPropertyValue(Pair<String, List<String>> property) {
        final List<String> values = property.getSecond();
        TypeConverter typeConverter = DefaultTypeConverter.INSTANCE;

        org.alfresco.service.namespace.QName alfQName = QName.createQName(property.getFirst());
        PropertyDefinition propertyDefinition = dictionaryService.getProperty(alfQName);

        if (!propertyDefinition.isMultiValued()) {
            if (values.size() == 0) {
                return null;
            }
            return (Serializable) typeConverter.convert(propertyDefinition.getDataType(), values.get(0));
        } else {
            return (Serializable) typeConverter.convert(propertyDefinition.getDataType(), values);
        }
    }
}