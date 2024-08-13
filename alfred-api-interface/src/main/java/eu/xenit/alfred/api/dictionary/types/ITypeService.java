package eu.xenit.alfred.api.dictionary.types;

import eu.xenit.alfred.api.data.QName;

/**
 * Gets type definitions.
 */
public interface ITypeService {

    /**
     * @param qname The qname of the type of which we want the subtype definitions.
     * @param follow true == follow up the super-class hierarchy, false == immediate sub types only
     * @return The sub types of the given type.
     */
    Types GetSubTypeDefinitions(QName qname, boolean follow);

    /**
     * @param qname The qname that represents the type.
     * @return The information (typedefinition) of a specific type.
     */
    TypeDefinition GetTypeDefinition(QName qname);
}