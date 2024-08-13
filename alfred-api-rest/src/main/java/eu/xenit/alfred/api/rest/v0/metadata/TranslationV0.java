package eu.xenit.alfred.api.rest.v0.metadata;

/**
 * Created by Michiel Huygen on 23/11/2015.
 */
public class TranslationV0 {

    // Value used in database
    private String value;
    // Translated value for user display
    private String displayLabel;

    public TranslationV0() {
    }

    public TranslationV0(String value, String displayLabel) {

        this.value = value;
        this.displayLabel = displayLabel;
    }

    public String getValue() {
        return value;
    }

    public String getDisplayLabel() {
        return displayLabel;
    }


    @Override
    public String toString() {
        return "Translation{" +
                "value='" + value + '\'' +
                ", displayLabel='" + displayLabel + '\'' +
                '}';
    }
}
