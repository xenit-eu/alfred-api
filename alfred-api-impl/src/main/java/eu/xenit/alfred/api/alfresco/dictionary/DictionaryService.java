package eu.xenit.alfred.api.alfresco.dictionary;


import eu.xenit.alfred.api.alfresco.AlfredApiToAlfrescoConversion;
import eu.xenit.alfred.api.dictionary.IDictionaryService;
import eu.xenit.alfred.api.dictionary.aspects.AspectDefinition;
import eu.xenit.alfred.api.dictionary.aspects.Aspects;
import eu.xenit.alfred.api.dictionary.aspects.IAspectService;
import eu.xenit.alfred.api.dictionary.namespaces.Namespace;
import eu.xenit.alfred.api.dictionary.namespaces.Namespaces;
import eu.xenit.alfred.api.dictionary.properties.IPropertyService;
import eu.xenit.alfred.api.dictionary.types.ITypeService;
import eu.xenit.alfred.api.dictionary.types.TypeDefinition;
import eu.xenit.alfred.api.dictionary.types.Types;
import eu.xenit.alfred.api.properties.Properties;
import eu.xenit.alfred.api.properties.PropertyDefinition;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.CRC32;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.ModelDefinition;
import org.alfresco.service.cmr.dictionary.NamespaceDefinition;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Created by Michiel Huygen on 24/11/2015.
 */

@Component("eu.xenit.alfred.api.dictionary.IDictionaryService")
public class DictionaryService implements IDictionaryService {

    private static final Logger logger = LoggerFactory.getLogger(DictionaryService.class);

    private AlfredApiToAlfrescoConversion c;
    private IPropertyService propertyService;
    private ITypeService typeService;
    private IAspectService aspectService;

    private org.alfresco.service.cmr.dictionary.DictionaryService dictionaryService;
    private NamespaceService namespaceService;

    @Autowired
    public DictionaryService(@Qualifier("ServiceRegistry") ServiceRegistry registry, AlfredApiToAlfrescoConversion alfredApiToAlfrescoConversion,
            IPropertyService propertyService, ITypeService typeService, IAspectService aspectService) {
        dictionaryService = registry.getDictionaryService();
        namespaceService = registry.getNamespaceService();
        c = alfredApiToAlfrescoConversion;
        this.propertyService = propertyService;
        this.typeService = typeService;
        this.aspectService = aspectService;
    }

    public Namespaces getNamespaces() {
        Map<String, Namespace> ret = new HashMap<>();
        Collection<String> uris = this.namespaceService.getURIs();
        for (String s : uris) {
            if (s == null || s.length() == 0) {
                continue;
            }
            ret.put(s, new Namespace(s, new ArrayList<>(this.namespaceService.getPrefixes(s))));
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
    public Types GetSubTypeDefinitions(eu.xenit.alfred.api.data.QName qname, boolean follow) {
        return typeService.GetSubTypeDefinitions(qname, follow);
    }

    @Override
    public TypeDefinition GetTypeDefinition(eu.xenit.alfred.api.data.QName qname) {
        return typeService.GetTypeDefinition(qname);
    }

    @Override
    public PropertyDefinition GetPropertyDefinition(eu.xenit.alfred.api.data.QName qname) {
        return propertyService.GetPropertyDefinition(qname);
    }

    @Override
    public Properties getProperties() {
        return propertyService.getProperties();
    }

    @Override
    public AspectDefinition GetAspectDefinition(eu.xenit.alfred.api.data.QName qname) {
        return aspectService.GetAspectDefinition(qname);
    }

    @Override
    public Aspects getAspects() {
        return aspectService.getAspects();
    }
}
