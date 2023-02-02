package eu.xenit.apix.alfresco.dictionary;


import eu.xenit.apix.alfresco.ApixToAlfrescoConversion;
import eu.xenit.apix.dictionary.IDictionaryService;
import eu.xenit.apix.dictionary.aspects.AspectDefinition;
import eu.xenit.apix.dictionary.aspects.Aspects;
import eu.xenit.apix.dictionary.aspects.IAspectService;
import eu.xenit.apix.dictionary.namespaces.Namespace;
import eu.xenit.apix.dictionary.namespaces.Namespaces;
import eu.xenit.apix.dictionary.properties.IPropertyService;
import eu.xenit.apix.dictionary.types.ITypeService;
import eu.xenit.apix.dictionary.types.TypeDefinition;
import eu.xenit.apix.dictionary.types.Types;
import eu.xenit.apix.properties.Properties;
import eu.xenit.apix.properties.PropertyDefinition;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.CRC32;
import org.alfresco.service.cmr.dictionary.ModelDefinition;
import org.alfresco.service.cmr.dictionary.NamespaceDefinition;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by Michiel Huygen on 24/11/2015.
 */

@Component("eu.xenit.apix.dictionary.IDictionaryService")
public class DictionaryService implements IDictionaryService {

    private final static Logger logger = LoggerFactory.getLogger(DictionaryService.class);
    @Autowired
    private ApixToAlfrescoConversion c;
    @Autowired
    private org.alfresco.service.cmr.dictionary.DictionaryService dictionaryService;

    @Autowired
    private IPropertyService propertyService;

    @Autowired
    private ITypeService typeService;

    @Autowired
    private IAspectService aspectService;

    @Autowired
    private NamespaceService namespaceService;

    public DictionaryService() {
    }

    public Namespaces getNamespaces() {
        Map<String, Namespace> ret = new HashMap<>();
        Collection<String> uris = this.namespaceService.getURIs();
        for (String s : uris) {
            if (s == null || s.length() == 0) {
                continue;
            }
            ret.put(s, new Namespace(s, new ArrayList<String>(this.namespaceService.getPrefixes(s))));
        }
        return new Namespaces(ret);
    }


    public long getContentModelCheckSum() {
        Collection<QName> models = dictionaryService
                .getAllModels(); // was dictionarydao.getmodels() => includes compiled models?
        final CRC32 crc = new CRC32();

        for (org.alfresco.service.namespace.QName modelName : models) {
            ModelDefinition modelDefinition = dictionaryService.getModel(modelName);
            Collection<NamespaceDefinition> namespaces = modelDefinition.getNamespaces();

            // Doesnt have to be closed since this does not unmanaged resources
            OutputStream stream = new OutputStream() {
                @Override
                public void write(int b) throws IOException {
                    crc.update(b);
                }
            };

            modelDefinition.toXML(ModelDefinition.XMLBindingType.DEFAULT, stream);
        }

        return crc.getValue();
    }


    @Override
    public Types GetSubTypeDefinitions(eu.xenit.apix.data.QName qname, boolean follow) {
        return typeService.GetSubTypeDefinitions(qname, follow);
    }

    @Override
    public TypeDefinition GetTypeDefinition(eu.xenit.apix.data.QName qname) {
        return typeService.GetTypeDefinition(qname);
    }

    @Override
    public PropertyDefinition GetPropertyDefinition(eu.xenit.apix.data.QName qname) {
        return propertyService.GetPropertyDefinition(qname);
    }

    @Override
    public Properties getProperties() {
        return propertyService.getProperties();
    }

    @Override
    public AspectDefinition GetAspectDefinition(eu.xenit.apix.data.QName qname) {
        return aspectService.GetAspectDefinition(qname);
    }

    @Override
    public Aspects getAspects() {
        return aspectService.getAspects();
    }
}
