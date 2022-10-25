package eu.xenit.apix.alfresco.properties;

import eu.xenit.apix.alfresco.ApixToAlfrescoConversion;
import eu.xenit.apix.dictionary.properties.IPropertyService;
import eu.xenit.apix.properties.Properties;
import eu.xenit.apix.properties.PropertyConstraintDefinition;
import eu.xenit.apix.properties.PropertyFacetable;
import eu.xenit.apix.properties.PropertyIndexOptions;
import eu.xenit.apix.properties.PropertyTokenised;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.alfresco.repo.dictionary.Facetable;
import org.alfresco.repo.dictionary.IndexTokenisationMode;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;


@Service("eu.xenit.apix.properties.IPropertyService")
@Primary
public class PropertyServiceImpl implements IPropertyService {

    private static final Logger logger = LoggerFactory.getLogger(PropertyServiceImpl.class);
    protected DictionaryService dictionaryService;
    private MessageService messageService;
    protected ApixToAlfrescoConversion c;

    @Autowired
    public PropertyServiceImpl(DictionaryService dictionaryService, ApixToAlfrescoConversion c,
            MessageService messageService) {
        this.dictionaryService = dictionaryService;
        this.c = c;
        this.messageService = messageService;
    }

    public PropertyIndexOptions GetPropertyIndexOptions(PropertyDefinition definition) {
        if (!definition.isIndexed()) {
            return null;
        }
        PropertyIndexOptions IndexOptions = new PropertyIndexOptions();
        IndexOptions.setFacetable(FacetableFromApix(definition.getFacetable()));
        IndexOptions.setStored(definition.isStoredInIndex());
        IndexTokenisationMode tokenisationMode = definition.getIndexTokenisationMode();
        IndexOptions.setTokenised(TokenisedFromApix(tokenisationMode));
        return IndexOptions;
    }

    private eu.xenit.apix.properties.PropertyDefinition propertyDefinitionConstructor(
            PropertyDefinition definition) {

        eu.xenit.apix.properties.PropertyDefinition propertyDefinitionUnderConstruction = new eu.xenit.apix.properties.PropertyDefinition(
                c.apix(definition.getName()), c.apix(definition.getContainerClass().getName()),
                definition.getTitle(messageService),
                definition.getDescription(messageService), definition.getDefaultValue(),
                c.apix(definition.getDataType().getName()), definition.isMultiValued(), definition.isMandatory(),
                definition.isMandatoryEnforced(), definition.isProtected(), this.GetPropertyIndexOptions(definition),
                this.GetConstraints(definition));

        return propertyDefinitionUnderConstruction;

    }

    public eu.xenit.apix.properties.PropertyDefinition GetPropertyDefinition(eu.xenit.apix.data.QName qname) {
        if (!IsValidPropertyQName(qname)) {
            logger.debug("The given property is no valid property: " + qname.toString());
            return null;
        }
        PropertyDefinition definition = dictionaryService.getProperty(c.alfresco(qname));
        return propertyDefinitionConstructor(definition);
    }


    public eu.xenit.apix.properties.PropertyDefinition GetPropertyDefinitionFromAlfrescoQname(
            org.alfresco.service.namespace.QName qname) {

        PropertyDefinition definition = dictionaryService.getProperty(qname);
        return propertyDefinitionConstructor(definition);

    }

    @Override
    public Properties getProperties() {

        Collection<org.alfresco.service.namespace.QName> properties = dictionaryService
                .getAllProperties(null);

        List<eu.xenit.apix.properties.PropertyDefinition> ret = new ArrayList<>(properties.size());
        for (org.alfresco.service.namespace.QName property : properties) {

            eu.xenit.apix.properties.PropertyDefinition propertyDefinition = GetPropertyDefinitionFromAlfrescoQname(
                    property);
            assert propertyDefinition != null;
            ret.add(propertyDefinition);

        }
        return new Properties(ret);

    }

    public boolean IsValidPropertyQName(eu.xenit.apix.data.QName qname) {
        try {
            if (!c.HasAlfrescoQname(qname)) {
                return false;
            }
            PropertyDefinition definition = dictionaryService.getProperty(c.alfresco(qname));
            return definition != null;
        } catch (Exception e) {
            logger.debug("Failed to retrieve property: " + qname);
            logger.debug(e.toString());
            return false;
        }
    }

    private List<PropertyConstraintDefinition> GetConstraints(PropertyDefinition definition) {
        List<PropertyConstraintDefinition> constraintsUnderConstruction = new ArrayList<>();
        for (ConstraintDefinition constraint : definition.getConstraints()) {
            PropertyConstraintDefinition constraintDefinition = new PropertyConstraintDefinition();
            constraintDefinition.setConstraintType(constraint.getConstraint().getType());
            constraintDefinition.setParameters(constraint.getConstraint().getParameters());
            constraintsUnderConstruction.add(constraintDefinition);
        }
        return constraintsUnderConstruction;
    }

    private static PropertyFacetable FacetableFromApix(Facetable facetable) {
        switch (facetable) {
            case FALSE:
                return PropertyFacetable.FALSE.FALSE;
            case TRUE:
                return PropertyFacetable.TRUE;
            default:
                return PropertyFacetable.DEFAULT;
        }
    }

    private PropertyTokenised TokenisedFromApix(IndexTokenisationMode tokMode) {
        switch (tokMode) {
            case FALSE:
                return PropertyTokenised.FALSE;
            case TRUE:
                return PropertyTokenised.TRUE;
            case BOTH:
                return PropertyTokenised.BOTH;
        }
        throw new Error("Unhandled type");
    }

}