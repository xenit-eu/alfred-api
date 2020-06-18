package eu.xenit.apix.alfresco.dictionary;

import eu.xenit.apix.alfresco.search.FtsNodeVisitor;
import eu.xenit.apix.data.QName;
import eu.xenit.apix.properties.PropertyDefinition;
import eu.xenit.apix.search.nodes.PropertySearchNode;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

public class PropertyTypeCheckService {

    private final Map<String, Function<String, Boolean>> constraintMap = new HashMap<String, Function<String, Boolean>>() {{
        put("d:int", PropertyTypeCheckService::isInt);
        put("{http://www.alfresco.org/model/dictionary/1.0}int", PropertyTypeCheckService::isInt);
        put("d:long", PropertyTypeCheckService::isLong);
        put("{http://www.alfresco.org/model/dictionary/1.0}long", PropertyTypeCheckService::isLong);
    }};

    private final PropertyService propertyService;

    public PropertyTypeCheckService(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    public boolean fitsType(PropertySearchNode node) {
        if (node == null || node.getValue() == null || node.getName() == null || propertyService == null) {
            return true;
        }
        QName qName = new QName(node.getName().replaceAll("\\\\", "")); //stuff like hyphen in node-dbid is escaped
        PropertyDefinition propertyDefinition = propertyService.GetPropertyDefinition(qName);
        if (propertyDefinition == null || propertyDefinition.getDataType() == null) {
            return true;
        }
        for (Entry<String, Function<String, Boolean>> entry : constraintMap.entrySet()) {
            if (entry.getKey().equals(propertyDefinition.getDataType().getValue())) {
                return entry.getValue().apply(node.getValue());
            }
        }
        return true;
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
