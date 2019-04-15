package eu.xenit.apix.properties;


public interface IPropertyService {

    /**
     * @param qname The qname of the requested property.
     * @return The definition of a property with a given qname.
     */
    PropertyDefinition GetPropertyDefinition(eu.xenit.apix.data.QName qname);
}