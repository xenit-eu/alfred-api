package eu.xenit.apix.alfresco42.properties;

import com.github.dynamicextensionsalfresco.osgi.OsgiService;
import eu.xenit.apix.alfresco.ApixToAlfrescoConversion;
import eu.xenit.apix.alfresco.properties.PropertyServiceBase;
import eu.xenit.apix.properties.*;
import org.alfresco.repo.dictionary.IndexTokenisationMode;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@OsgiService
@Component("eu.xenit.apix.properties.IPropertyService")
@Primary
public class PropertyServiceImpl42 extends PropertyServiceBase {

    @Autowired
    public PropertyServiceImpl42(DictionaryService dictionaryService, ApixToAlfrescoConversion c) {
        super(dictionaryService, c);
    }

    @Override
    public PropertyIndexOptions GetPropertyIndexOptions(PropertyDefinition definition) {
        if (!definition.isIndexed()) {
            return null;
        }
        PropertyIndexOptions IndexOptions = new PropertyIndexOptions();
        IndexOptions.setFacetable(PropertyFacetable.DEFAULT);
        IndexOptions.setStored(definition.isStoredInIndex());
        IndexTokenisationMode tokenisationMode = definition.getIndexTokenisationMode();
        IndexOptions.setTokenised(this.TokenisedFromApix(tokenisationMode));
        return IndexOptions;
    }
}
