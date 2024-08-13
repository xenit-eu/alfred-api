package eu.xenit.alfred.api.workflow.search;

import java.util.HashMap;
import java.util.Map;

public class Facets {

    public Map<String, Facet> facets;

    public Facets() {
        this.facets = new HashMap<>();
    }

    public Facets(Map<String, Facet> facets) {
        this.facets = facets;
    }

    public void CheckValidity() {
        for (String s : facets.keySet()) {
            if (s == null) {
                throw new Error("Key of facet cannot be null");
            }
            Facet f = facets.get(s);
            for (String v : f.getValues().keySet()) {
                if (v == null) {
                    throw new Error("Value of facet with key " + s + "of facet cannot be null");
                }
            }
        }
    }

    public Map<String, Facet> getFacets() {
        return facets;
    }

    public void setFacets(Map<String, Facet> facets) {
        this.facets = facets;
    }

    public void AddValue(String property, String value) {
        Facet f = facets.get(property);
        if (f == null) {
            f = new Facet(property, new HashMap<String, FacetValue>());
            facets.put(property, f);
        }
        f.AddValue(value);
    }
}
