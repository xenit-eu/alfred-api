package eu.xenit.apix.alfresco51;

import com.github.dynamicextensionsalfresco.osgi.OsgiService;
import eu.xenit.apix.alfresco.ApixToAlfrescoConversion;
import eu.xenit.apix.alfresco.properties.PropertyServiceBase;
import eu.xenit.apix.properties.PropertyFacetable;
import eu.xenit.apix.properties.PropertyIndexOptions;
import org.alfresco.repo.dictionary.Facetable;
import org.alfresco.repo.dictionary.IndexTokenisationMode;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@OsgiService
@Component("eu.xenit.apix.properties.IPropertyService")
@Primary
public class PropertyServiceImpl51 extends PropertyServiceBase {

    @Autowired
    public PropertyServiceImpl51(DictionaryService dictionaryService, ApixToAlfrescoConversion c) {
        super(dictionaryService, c);
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

    @Override
    public PropertyIndexOptions GetPropertyIndexOptions(PropertyDefinition definition) {
        if (!definition.isIndexed()) {
            return null;
        }
        PropertyIndexOptions IndexOptions = new PropertyIndexOptions();
        IndexOptions.setFacetable(this.FacetableFromApix(definition.getFacetable()));
        IndexOptions.setStored(definition.isStoredInIndex());
        IndexTokenisationMode tokenisationMode = definition.getIndexTokenisationMode();
        IndexOptions.setTokenised(this.TokenisedFromApix(tokenisationMode));
        return IndexOptions;
    }
}
