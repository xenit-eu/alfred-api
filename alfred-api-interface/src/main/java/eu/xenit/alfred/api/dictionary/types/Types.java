package eu.xenit.alfred.api.dictionary.types;

import java.util.ArrayList;
import java.util.List;

/**
 * Represent a list of types. The list contains the typedefinitions and not only the qnames.
 */
public class Types {

    private List<TypeDefinition> types;

    public Types() {
        this(new ArrayList<TypeDefinition>());
    }

    public Types(List<TypeDefinition> types) {
        this.types = types;
    }

    public List<TypeDefinition> getTypes() {
        return types;
    }

    public void setTypes(List<TypeDefinition> types) {
        this.types = types;
    }
}
