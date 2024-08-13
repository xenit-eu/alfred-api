package eu.xenit.alfred.api.alfresco.dictionary;

import eu.xenit.alfred.api.alfresco.AlfredApiToAlfrescoConversion;
import eu.xenit.alfred.api.data.QName;
import eu.xenit.alfred.api.dictionary.types.ITypeService;
import eu.xenit.alfred.api.dictionary.types.TypeDefinition;
import eu.xenit.alfred.api.dictionary.types.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("eu.xenit.alfred.api.dictionary.types.ITypeService")
public class TypeService implements ITypeService {

    private static final Logger logger = LoggerFactory.getLogger(TypeService.class);
    private DictionaryService dictionaryService;
    private AlfredApiToAlfrescoConversion c;

    @Autowired
    public TypeService(DictionaryService dictionaryService, AlfredApiToAlfrescoConversion c) {
        this.dictionaryService = dictionaryService;
        this.c = c;
    }

    @Override
    public Types GetSubTypeDefinitions(QName parent, boolean follow) {
        Collection<org.alfresco.service.namespace.QName> typeNames = dictionaryService
                .getSubTypes(c.alfresco(parent), follow);
        List<TypeDefinition> ret = new ArrayList<>(typeNames.size());

        for (org.alfresco.service.namespace.QName typeName : typeNames) {
            TypeDefinition typeDef = GetTypeDefinition(typeName);

            // subtype-names were returned by the dictionary-service
            // and should always be resolvable
            assert typeDef != null;
            ret.add(typeDef);
        }
        return new Types(ret);
    }

    private TypeDefinition GetTypeDefinition(org.alfresco.service.namespace.QName qname) {
        org.alfresco.service.cmr.dictionary.TypeDefinition typeDef = dictionaryService.getType(qname);
        if (typeDef == null) {
            return null;
        }

        TypeDefinition ret = new TypeDefinition();
        ret.setName(c.alfredApi(typeDef.getName()));
        if (typeDef.getParentName() != null) {
            ret.setParent(c.alfredApi(typeDef.getParentName()));
        }
        ret.setDescription(typeDef.getDescription(dictionaryService));
        ret.setTitle(typeDef.getTitle(dictionaryService));
        List<QName> properties = new ArrayList<>();
        for (org.alfresco.service.namespace.QName qName : typeDef.getProperties().keySet()) {
            properties.add(c.alfredApi((qName)));
        }
        ret.setProperties(properties);
        List<QName> mandatoryAspects = typeDef.getDefaultAspects().stream()
                .map(aspectDefinition -> c.alfredApi(aspectDefinition.getName()))
                .collect(Collectors.toList());
        ret.setMandatoryAspects(mandatoryAspects);
        return ret;

    }

    @Override
    public TypeDefinition GetTypeDefinition(QName qname) {
        TypeDefinition typeDef = null;
        org.alfresco.service.namespace.QName alfQName = null;
        try {
            alfQName = c.alfresco(qname);
            typeDef = GetTypeDefinition(alfQName);
        } catch (NamespaceException namespaceException) {
            logger.warn("Failed to create alfresco qname for {}, returning null", qname);
            typeDef = null;
        }
        return typeDef;
    }
}
