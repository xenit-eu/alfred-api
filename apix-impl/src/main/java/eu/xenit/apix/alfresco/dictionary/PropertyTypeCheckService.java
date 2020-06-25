package eu.xenit.apix.alfresco.dictionary;

import eu.xenit.apix.data.QName;
import eu.xenit.apix.properties.PropertyDefinition;
import eu.xenit.apix.search.nodes.PropertySearchNode;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertyTypeCheckService {

    private final static Logger logger = LoggerFactory.getLogger(PropertyTypeCheckService.class);

    private final Map<String, Predicate<String>> dataTypePredicates = new HashMap<String, Predicate<String>>() {{
        put("d:int", PropertyTypeCheckService::isInt);
        put("{http://www.alfresco.org/model/dictionary/1.0}int", PropertyTypeCheckService::isInt);
        put("d:long", PropertyTypeCheckService::isLong);
        put("{http://www.alfresco.org/model/dictionary/1.0}long", PropertyTypeCheckService::isLong);
    }};

    private final PropertyService propertyService;

    public PropertyTypeCheckService(PropertyService propertyService) {
        if (propertyService == null) {
            logger.warn("Instantiating PropertyTypeCheckService without a PropertyService, type checks will always succeed");
        }
        this.propertyService = propertyService;
    }

    public boolean fitsType(PropertySearchNode node) {
        if (node == null || node.getValue() == null || node.getName() == null || propertyService == null) {
            return true;
        }
        QName qName = new QName(PropertySearchNode.unescapeName(node.getName()));
        PropertyDefinition propertyDefinition = propertyService.GetPropertyDefinition(qName);
        if (propertyDefinition == null || propertyDefinition.getDataType() == null) {
            return true;
        }
        Predicate<String> predicate = dataTypePredicates.get(propertyDefinition.getDataType().getValue());
        if (predicate == null) {
            return true;
        }
        return predicate.test(node.getValue());
    }

    private static boolean isInt(String value) {
        try {
            Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    private static boolean isLong(String value) {
        try {
            Long.parseLong(value);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

}
