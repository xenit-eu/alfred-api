package eu.xenit.alfred.api.translation;

import java.util.List;

/**
 * Datastructure that represents the translations of a specific language. types: The available translations of the
 * types. aspects: The available translations of the aspects. association: The available translations of the
 * association. properties: The available translations of the properties.
 */
public class Translations {

    private List<TranslationValue> types;
    private List<TranslationValue> aspects;
    private List<TranslationValue> association;
    private List<PropertyTranslationValue> properties;

    public Translations() {
    }

    public Translations(List<TranslationValue> types, List<TranslationValue> aspects,
            List<TranslationValue> association, List<PropertyTranslationValue> properties) {
        this.types = types;
        this.aspects = aspects;
        this.association = association;
        this.properties = properties;
    }

    @Override
    public String toString() {
        return "Translations{" +
                "types=" + types +
                ", aspects=" + aspects +
                ", association=" + association +
                ", properties=" + properties +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Translations that = (Translations) o;

        if (getTypes() != null ? !getTypes().equals(that.getTypes()) : that.getTypes() != null) {
            return false;
        }
        if (getAspects() != null ? !getAspects().equals(that.getAspects()) : that.getAspects() != null) {
            return false;
        }
        if (getAssociation() != null ? !getAssociation().equals(that.getAssociation())
                : that.getAssociation() != null) {
            return false;
        }
        return getProperties() != null ? getProperties().equals(that.getProperties()) : that.getProperties() == null;

    }

    @Override
    public int hashCode() {
        int result = getTypes() != null ? getTypes().hashCode() : 0;
        result = 31 * result + (getAspects() != null ? getAspects().hashCode() : 0);
        result = 31 * result + (getAssociation() != null ? getAssociation().hashCode() : 0);
        result = 31 * result + (getProperties() != null ? getProperties().hashCode() : 0);
        return result;
    }

    public List<TranslationValue> getTypes() {
        return types;
    }

    public void setTypes(List<TranslationValue> types) {
        this.types = types;
    }

    public List<TranslationValue> getAspects() {
        return aspects;
    }

    public void setAspects(List<TranslationValue> aspects) {
        this.aspects = aspects;
    }

    public List<TranslationValue> getAssociation() {
        return association;
    }

    public void setAssociation(List<TranslationValue> association) {
        this.association = association;
    }

    public List<PropertyTranslationValue> getProperties() {
        return properties;
    }

    public void setProperties(List<PropertyTranslationValue> properties) {
        this.properties = properties;
    }
}
