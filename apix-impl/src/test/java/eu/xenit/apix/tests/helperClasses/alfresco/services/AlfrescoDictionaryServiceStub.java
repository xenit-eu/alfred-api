package eu.xenit.apix.tests.helperClasses.alfresco.services;

import eu.xenit.apix.alfresco.ApixToAlfrescoConversion;
import eu.xenit.apix.tests.helperClasses.alfresco.definitions.AlfrescoAspectDefinitionStub;
import eu.xenit.apix.tests.helperClasses.alfresco.definitions.AlfrescoTypeDefinitionStub;
import eu.xenit.apix.tests.helperClasses.apix.ApixTypeDefinitionStub;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.ModelDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.namespace.QName;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;

public class AlfrescoDictionaryServiceStub implements DictionaryService {

    private Map<eu.xenit.apix.data.QName, eu.xenit.apix.dictionary.aspects.AspectDefinition> aspectDefs;
    private Map<eu.xenit.apix.data.QName, eu.xenit.apix.properties.PropertyDefinition> propertyDefs;
    private Map<eu.xenit.apix.data.QName, eu.xenit.apix.dictionary.types.TypeDefinition> typeDefs;
    private ApixToAlfrescoConversion apixAlfrescoConversion;

    public AlfrescoDictionaryServiceStub(
            Map<eu.xenit.apix.data.QName, eu.xenit.apix.dictionary.aspects.AspectDefinition> aspectDefs,
            Map<eu.xenit.apix.data.QName, eu.xenit.apix.properties.PropertyDefinition> propertyDefs,
            Map<eu.xenit.apix.data.QName, eu.xenit.apix.dictionary.types.TypeDefinition> typeDefs,
            ApixToAlfrescoConversion apixAlfrescoConverter) {
        this.aspectDefs = aspectDefs;
        this.propertyDefs = propertyDefs;
        this.typeDefs = typeDefs;
        this.apixAlfrescoConversion = apixAlfrescoConverter;
    }

    @Override
    public Collection<QName> getAllModels() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ModelDefinition getModel(QName model) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<QName> getAllDataTypes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<QName> getDataTypes(QName model) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DataTypeDefinition getDataType(QName name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DataTypeDefinition getDataType(Class<?> javaClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<QName> getAllTypes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<QName> getAllTypes(boolean includeInherited) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<QName> getSubTypes(QName type, boolean follow) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<QName> getTypes(QName model) {
        throw new UnsupportedOperationException();
    }

    @Override
    public TypeDefinition getType(QName name) {
        eu.xenit.apix.data.QName apixQName = apixAlfrescoConversion.apix(name);
        eu.xenit.apix.dictionary.types.TypeDefinition typeDef = typeDefs.get(apixQName);

        ApixTypeDefinitionStub apixTypeDef = (ApixTypeDefinitionStub) typeDef;

        return new AlfrescoTypeDefinitionStub(apixQName, apixTypeDef.getParent(), apixTypeDef.getAspects(),
                apixAlfrescoConversion);
    }

    @Override
    public TypeDefinition getAnonymousType(QName type, Collection<QName> aspects) {
        throw new UnsupportedOperationException();
    }

    @Override
    public TypeDefinition getAnonymousType(QName name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<QName> getAllAspects() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<QName> getAllAspects(boolean includeInherited) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<QName> getSubAspects(QName aspect, boolean follow) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<QName> getAspects(QName model) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<QName> getAssociations(QName model) {
        throw new UnsupportedOperationException();
    }

    @Override
    public AspectDefinition getAspect(QName name) {
        eu.xenit.apix.data.QName apixQName = apixAlfrescoConversion.apix(name);
        eu.xenit.apix.dictionary.aspects.AspectDefinition apixAspectDef = aspectDefs.get(apixQName);

        return new AlfrescoAspectDefinitionStub(apixAspectDef.getName(), apixAlfrescoConversion);
    }

    @Override
    public ClassDefinition getClass(QName name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSubClass(QName className, QName ofClassName) {
        eu.xenit.apix.data.QName apixClassName = apixAlfrescoConversion.apix(className);
        if (typeDefs.containsKey(apixClassName)) {
            eu.xenit.apix.dictionary.types.TypeDefinition typeDef = typeDefs.get(apixClassName);
            return isSubTypeOfImpl(typeDef, apixAlfrescoConversion.apix(ofClassName));
        }

        return false;
    }

    private boolean isSubTypeOfImpl(eu.xenit.apix.dictionary.types.TypeDefinition typeDef,
            eu.xenit.apix.data.QName ofType) {
        return typeDef.getName() == ofType || isSubTypeOfRecursive(typeDef, ofType);
    }

    private boolean isSubTypeOfRecursive(eu.xenit.apix.dictionary.types.TypeDefinition typeDef,
            eu.xenit.apix.data.QName ofType) {
        eu.xenit.apix.data.QName parentType = typeDef.getParent();
        if (parentType == null) {
            return false;
        }

        eu.xenit.apix.dictionary.types.TypeDefinition parentTypeDef = typeDefs.get(parentType);
        if (parentTypeDef == null) {
            return false;
        }

        if (parentTypeDef.getName().toString().equals(ofType.toString())) {
            return true;
        } else if (parentTypeDef.getParent() == null) {
            return false;
        } else {
            return isSubTypeOfRecursive(parentTypeDef, ofType);
        }
    }

    @Override
    public PropertyDefinition getProperty(QName className, QName propertyName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<QName, PropertyDefinition> getPropertyDefs(QName className) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PropertyDefinition getProperty(QName propertyName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<QName> getAllProperties(QName dataType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<QName> getProperties(QName model, QName dataType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<QName> getProperties(QName model) {
        throw new UnsupportedOperationException();
    }

    @Override
    public AssociationDefinition getAssociation(QName associationName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<QName> getAllAssociations() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<QName> getAllAssociations(boolean includeInherited) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ConstraintDefinition getConstraint(QName constraintQName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<ConstraintDefinition> getConstraints(QName model) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<ConstraintDefinition> getConstraints(QName model, boolean referenceableDefsOnly) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getMessage(String messageKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getMessage(String messageKey, Locale locale) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getMessage(String messageKey, Object... params) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getMessage(String messageKey, Locale locale, Object... params) {
        throw new UnsupportedOperationException();
    }
}
