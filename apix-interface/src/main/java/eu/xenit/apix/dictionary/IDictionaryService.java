package eu.xenit.apix.dictionary;


import eu.xenit.apix.dictionary.aspects.IAspectService;
import eu.xenit.apix.dictionary.namespaces.Namespaces;
import eu.xenit.apix.dictionary.properties.IPropertyService;
import eu.xenit.apix.dictionary.types.ITypeService;

/**
 * Aggregates all operations of the property service and the typeservice.
 */
public interface IDictionaryService extends IPropertyService, ITypeService, IAspectService {

    /**
     * @return The checksum of the content model. This is used to check if the content model changed since last call
     * without checking the whole model.
     */
    long getContentModelCheckSum();

    /**
     * @return The namespaces of the content model.
     */
    Namespaces getNamespaces();

}
