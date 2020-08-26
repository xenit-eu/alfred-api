package eu.xenit.apix.dictionary.aspects;

import java.util.List;

public class Aspects {

    private List<AspectDefinition> aspects;

    public Aspects() {
    }

    public Aspects(List<AspectDefinition> aspects) {
        this.aspects = aspects;
    }

    public List<AspectDefinition> getAspects() {
        return aspects;
    }

    public void setAspects(List<AspectDefinition> aspects) {
        this.aspects = aspects;
    }
}
