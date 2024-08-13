package eu.xenit.alfred.api.properties;


import eu.xenit.alfred.api.data.QName;

public interface IPropertyService {

    /**
     * @param qname The qname of the requested property.
     * @return The definition of a property with a given qname.
     */
    PropertyDefinition GetPropertyDefinition(QName qname);

    Properties getProperties();

}