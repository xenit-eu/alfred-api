package eu.xenit.apix.translation;

import eu.xenit.apix.data.QName;
import io.swagger.annotations.ApiModelProperty;
//import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

/**
 * Datastructure that represents the translations of a single property its values for a single language. values: The map
 * from property key to translation.
 */
public class PropertyTranslationValue extends TranslationValue {

//    @Schema(type = "Map[string,string]")
    @ApiModelProperty(dataType = "Map[string,string]")
    private Map<String, String> values;

    public PropertyTranslationValue() {
    }

    public PropertyTranslationValue(QName qname, String title, String description, Map<String, String> values) {
        super(qname, title, description);
        this.values = values;
    }

    @Override
    public String toString() {
        return "PropertyTranslationValue{" +
                super.toString() +
                "values=" + values +
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
        if (!super.equals(o)) {
            return false;
        }

        PropertyTranslationValue that = (PropertyTranslationValue) o;

        return values != null ? values.equals(that.values) : that.values == null;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (values != null ? values.hashCode() : 0);
        return result;
    }

    public Map<String, String> getValues() {
        return values;
    }

    public void setValues(Map<String, String> values) {
        this.values = values;
    }
}
