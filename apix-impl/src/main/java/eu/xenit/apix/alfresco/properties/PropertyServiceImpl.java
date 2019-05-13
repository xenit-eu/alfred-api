package eu.xenit.apix.alfresco.properties;

import eu.xenit.apix.alfresco.ApixToAlfrescoConversion;
import eu.xenit.apix.dictionary.properties.IPropertyService;
import eu.xenit.apix.properties.PropertyConstraintDefinition;
import eu.xenit.apix.properties.PropertyIndexOptions;
import eu.xenit.apix.properties.PropertyTokenised;
import java.util.ArrayList;
import java.util.List;
import org.alfresco.repo.dictionary.IndexTokenisationMode;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class PropertyServiceImpl implements IPropertyService {

    private static final Logger logger = LoggerFactory.getLogger(PropertyServiceImpl.class);
    protected DictionaryService dictionaryService;
    protected ApixToAlfrescoConversion c;

    public PropertyServiceImpl(DictionaryService dictionaryService, ApixToAlfrescoConversion c) {
        this.dictionaryService = dictionaryService;
        this.c = c;
    }


    public eu.xenit.apix.properties.PropertyDefinition GetPropertyDefinition(eu.xenit.apix.data.QName qname) {
        if (!IsValidPropertyQName(qname)) {
            logger.info("The given property is no valid property: " + qname.toString());
            return null;
        }
        PropertyDefinition definition = dictionaryService.getProperty(c.alfresco(qname));
        eu.xenit.apix.properties.PropertyDefinition propertyDefinitionUnderConstruction = new eu.xenit.apix.properties.PropertyDefinition();
        propertyDefinitionUnderConstruction.setName(c.apix(definition.getName()));
        propertyDefinitionUnderConstruction.setContainer(c.apix(definition.getContainerClass().getName()));
        propertyDefinitionUnderConstruction.setTitle(definition.getTitle());
        propertyDefinitionUnderConstruction.setDescription(definition.getDescription());
        propertyDefinitionUnderConstruction.setDefaultValue(definition.getDefaultValue());
        propertyDefinitionUnderConstruction.setDataType(c.apix(definition.getDataType().getName()));
        propertyDefinitionUnderConstruction.setMultiValued(definition.isMultiValued());
        propertyDefinitionUnderConstruction.setMandatory(definition.isMandatory());
        propertyDefinitionUnderConstruction.setEnforced(definition.isMandatoryEnforced());
        propertyDefinitionUnderConstruction.setIsProtected(definition.isProtected());
        propertyDefinitionUnderConstruction.setIndexed(this.GetPropertyIndexOptions(definition));
        propertyDefinitionUnderConstruction.setConstraints(this.GetConstraints(definition));
        return propertyDefinitionUnderConstruction;
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

    public abstract PropertyIndexOptions GetPropertyIndexOptions(PropertyDefinition definition);

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

    protected PropertyTokenised TokenisedFromApix(IndexTokenisationMode tokMode) {
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