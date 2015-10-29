package eu.xenit.apix.tests.helperClasses.apix;

import eu.xenit.apix.data.QName;
import eu.xenit.apix.dictionary.types.TypeDefinition;
import java.util.List;

public class ApixTypeDefinitionStub extends TypeDefinition{
    private List<QName> aspects;

    public List<QName> getAspects() {
        return aspects;
    }

    public void setAspects(List<QName> aspects) {
        this.aspects = aspects;
    }
}
