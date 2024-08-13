package eu.xenit.alfred.api.properties;

/**
 * Information around the indexation with a property.
 * https://community.alfresco.com/docs/DOC-4798-full-text-search-configuration
 */
public class PropertyIndexOptions {

    private boolean stored;
    private PropertyTokenised tokenised;
    private PropertyFacetable facetable;

    public PropertyIndexOptions() {

    }

    public PropertyTokenised getTokenised() {
        return tokenised;
    }

    public void setTokenised(PropertyTokenised tokenised) {
        this.tokenised = tokenised;
    }

    public PropertyFacetable getFacetable() {
        return facetable;
    }

    public void setFacetable(PropertyFacetable facetable) {
        this.facetable = facetable;
    }

    public boolean isStored() {
        return stored;
    }

    public void setStored(boolean stored) {
        this.stored = stored;
    }

}

