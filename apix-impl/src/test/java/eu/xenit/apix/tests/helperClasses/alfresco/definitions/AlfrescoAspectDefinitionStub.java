package eu.xenit.apix.tests.helperClasses.alfresco.definitions;

import eu.xenit.apix.alfresco.ApixToAlfrescoConversion;
import org.alfresco.service.cmr.dictionary.*;
import org.alfresco.service.cmr.i18n.MessageLookup;
import org.alfresco.service.namespace.QName;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AlfrescoAspectDefinitionStub implements AspectDefinition {

    private eu.xenit.apix.data.QName qName;
    private ApixToAlfrescoConversion apixAlfrescoConversion;

    public AlfrescoAspectDefinitionStub(eu.xenit.apix.data.QName qName, ApixToAlfrescoConversion apixAlfrescoConversion) {
        this.qName = qName;
        this.apixAlfrescoConversion = apixAlfrescoConversion;
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
        return null;
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
        return new HashMap<QName, PropertyDefinition>();
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
        return null;
    }

    @Override
    public Set<QName> getDefaultAspectNames() {
        return null;
    }

    @Override
    public List<AspectDefinition> getDefaultAspects(boolean inherited) {
        return null;
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
