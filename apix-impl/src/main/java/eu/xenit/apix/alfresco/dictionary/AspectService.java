package eu.xenit.apix.alfresco.dictionary;

import com.github.dynamicextensionsalfresco.osgi.OsgiService;
import eu.xenit.apix.alfresco.ApixToAlfrescoConversion;
import eu.xenit.apix.data.QName;
import eu.xenit.apix.dictionary.aspects.AspectDefinition;
import eu.xenit.apix.dictionary.aspects.Aspects;
import eu.xenit.apix.dictionary.aspects.IAspectService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@OsgiService
@Component("eu.xenit.apix.dictionary.aspects.IAspectService")
public class AspectService implements IAspectService {

    private static final Logger logger = LoggerFactory.getLogger(AspectService.class);
    private DictionaryService dictionaryService;
    private ApixToAlfrescoConversion c;

    @Autowired
    public AspectService(DictionaryService dictionaryService, ApixToAlfrescoConversion c) {
        this.dictionaryService = dictionaryService;
        this.c = c;
    }

    private AspectDefinition GetAspectDefinition(org.alfresco.service.namespace.QName qname) {
        if (!HasAspectDefinition(qname)) {
            return null;
        }
        org.alfresco.service.cmr.dictionary.AspectDefinition aspectDef = dictionaryService.getAspect(qname);
        AspectDefinition ret = new AspectDefinition();
        ret.setName(c.apix(aspectDef.getName()));
        if (aspectDef.getParentName() != null) {
            ret.setParent(c.apix(aspectDef.getParentName()));
        }
        ret.setDescription(aspectDef.getDescription(dictionaryService));
        ret.setTitle(aspectDef.getTitle(dictionaryService));
        List<QName> properties = new ArrayList<>();
        for (org.alfresco.service.namespace.QName qName : aspectDef.getProperties().keySet()) {
            properties.add(c.apix((qName)));
        }
        ret.setProperties(properties);
        List<QName> mandatoryAspects = aspectDef.getDefaultAspects(true).stream()
                .map(aspectDefinition -> c.apix(aspectDefinition.getName()))
                .collect(Collectors.toList());
        ret.setMandatoryAspects(mandatoryAspects);
        return ret;

    }

    @Override
    public AspectDefinition GetAspectDefinition(QName qname) {
        if (!c.HasAlfrescoQname(qname)) {
            return null;
        }
        return GetAspectDefinition(c.alfresco((qname)));
    }


    @Override
    public Aspects getAspects() {
        Collection<org.alfresco.service.namespace.QName> aspects = dictionaryService
                .getAllAspects();
        List<AspectDefinition> ret = new ArrayList<>(aspects.size());
        for (org.alfresco.service.namespace.QName aspect : aspects) {
            AspectDefinition aspectDefinition = GetAspectDefinition(aspect);
            assert aspectDefinition != null;
            ret.add(aspectDefinition);
        }
        return new Aspects(ret);
    }

    private boolean HasAspectDefinition(org.alfresco.service.namespace.QName qname) {
        try {
            if (!c.HasApixQname(qname)) {
                return false;
            }
            org.alfresco.service.cmr.dictionary.AspectDefinition aspectDef = dictionaryService.getAspect(qname);
            return aspectDef != null;
        } catch (Exception e) {
            logger.debug("Checking has aspect definition failed");
            logger.debug(e.toString());
            return false;
        }
    }
}
