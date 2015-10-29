package eu.xenit.apix.tests.helperClasses.alfresco.definitions;

import eu.xenit.apix.alfresco.ApixToAlfrescoConversion;
import org.alfresco.service.cmr.dictionary.*;
import org.alfresco.service.cmr.i18n.MessageLookup;
import org.alfresco.service.namespace.QName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AlfrescoTypeDefinitionStub implements TypeDefinition {

    private ApixToAlfrescoConversion apixAlfrescoConversion;
    private eu.xenit.apix.data.QName qName;
    private eu.xenit.apix.data.QName parentQName;
    private List<eu.xenit.apix.data.QName> defaultAspects;

    public AlfrescoTypeDefinitionStub(eu.xenit.apix.data.QName qName, eu.xenit.apix.data.QName parentQName,
            List<eu.xenit.apix.data.QName> defaultAspects, ApixToAlfrescoConversion apixAlfrescoConversion) {
        this.apixAlfrescoConversion = apixAlfrescoConversion;
        this.qName = qName;
        this.parentQName = parentQName;
        this.defaultAspects = defaultAspects;
    }

    @Override
    public ModelDefinition getModel() {
        return null;
    }

    @Override
    public QName getName() {
        return apixAlfrescoConversion.alfresco(qName);
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String getTitle(MessageLookup messageLookup) {
        return null;
    }

    @Override
    public String getDescription(MessageLookup messageLookup) {
        return null;
    }

    @Override
    public QName getParentName() {
        return apixAlfrescoConversion.alfresco(parentQName);
    }

    @Override
    public boolean isAspect() {
        return false;
    }

    @Override
    public Boolean getArchive() {
        return null;
    }

    @Override
    public Boolean getIncludedInSuperTypeQuery() {
        return null;
    }

    @Override
    public Map<QName, PropertyDefinition> getProperties() {
        return null;
    }

    @Override
    public Map<QName, Serializable> getDefaultValues() {
        return null;
    }

    @Override
    public Map<QName, AssociationDefinition> getAssociations() {
        return null;
    }

    @Override
    public boolean isContainer() {
        return false;
    }

    @Override
    public Map<QName, ChildAssociationDefinition> getChildAssociations() {
        return null;
    }

    @Override
    public List<AspectDefinition> getDefaultAspects() {
        List<AspectDefinition> alfrescoAspectDefinitions = new ArrayList<>();
        List<eu.xenit.apix.data.QName> apixAspects = defaultAspects;
        for (eu.xenit.apix.data.QName apixAspect : apixAspects) {
            alfrescoAspectDefinitions.add(new AlfrescoAspectDefinitionStub(apixAspect, apixAlfrescoConversion));
        }

        return alfrescoAspectDefinitions;
    }

    @Override
    public Set<QName> getDefaultAspectNames() {
        return null;
    }

    @Override
    public List<AspectDefinition> getDefaultAspects(boolean inherited) {
        List<AspectDefinition> alfrescoDefaultAspectDefinitions = new ArrayList<>();
        List<eu.xenit.apix.data.QName> apixDefaultAspects = defaultAspects;
        for (eu.xenit.apix.data.QName apixDefaultAspect : apixDefaultAspects) {
            alfrescoDefaultAspectDefinitions
                    .add(new AlfrescoAspectDefinitionStub(apixDefaultAspect, apixAlfrescoConversion));
        }

        return alfrescoDefaultAspectDefinitions;
    }

    @Override
    public String getAnalyserResourceBundleName() {
        return null;
    }

    @Override
    public ClassDefinition getParentClassDefinition() {
        return null;
    }
}
