package eu.xenit.apix.dictionary.properties;

import eu.xenit.apix.properties.Properties;
import eu.xenit.apix.properties.PropertyDefinition;

public interface IPropertyService extends eu.xenit.apix.properties.IPropertyService {

    /**
     * @param qname The qname of the requested property.
     * @return The definition of a property with a given qname.
     */
    PropertyDefinition GetPropertyDefinition(eu.xenit.apix.data.QName qname);

    Properties getProperties();


}