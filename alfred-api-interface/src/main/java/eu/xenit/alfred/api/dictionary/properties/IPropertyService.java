package eu.xenit.alfred.api.dictionary.properties;

import eu.xenit.alfred.api.data.QName;
import eu.xenit.alfred.api.properties.Properties;
import eu.xenit.alfred.api.properties.PropertyDefinition;

public interface IPropertyService extends eu.xenit.alfred.api.properties.IPropertyService {

    /**
     * @param qname The qname of the requested property.
     * @return The definition of a property with a given qname.
     */
    PropertyDefinition GetPropertyDefinition(QName qname);

    Properties getProperties();


}