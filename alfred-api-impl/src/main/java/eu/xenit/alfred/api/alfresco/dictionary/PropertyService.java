package eu.xenit.alfred.api.alfresco.dictionary;

import eu.xenit.alfred.api.data.QName;
import eu.xenit.alfred.api.dictionary.properties.IPropertyService;
import eu.xenit.alfred.api.properties.Properties;
import eu.xenit.alfred.api.properties.PropertyDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;


@Component("eu.xenit.alfred.api.dictionary.properties.IPropertyService")
public class PropertyService implements IPropertyService {

    @Autowired
    @Qualifier("eu.xenit.alfred.api.properties.IPropertyService")
    eu.xenit.alfred.api.properties.IPropertyService parentService;

    @Override
    public PropertyDefinition GetPropertyDefinition(QName qname) {
        return parentService.GetPropertyDefinition(qname);
    }


    @Override
    public Properties getProperties() {
        return parentService.getProperties();
    }

}
