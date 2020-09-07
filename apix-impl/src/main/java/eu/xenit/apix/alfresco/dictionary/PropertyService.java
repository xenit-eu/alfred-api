package eu.xenit.apix.alfresco.dictionary;

import eu.xenit.apix.data.QName;
import eu.xenit.apix.dictionary.properties.IPropertyService;
import eu.xenit.apix.properties.Properties;
import eu.xenit.apix.properties.PropertyDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;


@Component("eu.xenit.apix.dictionary.properties.IPropertyService")
public class PropertyService implements IPropertyService {

    @Autowired
    @Qualifier("eu.xenit.apix.properties.IPropertyService")
    eu.xenit.apix.properties.IPropertyService parentService;

    @Override
    public PropertyDefinition GetPropertyDefinition(QName qname) {
        return parentService.GetPropertyDefinition(qname);
    }


    @Override
    public Properties getProperties() {
        return parentService.getProperties();
    }

}
