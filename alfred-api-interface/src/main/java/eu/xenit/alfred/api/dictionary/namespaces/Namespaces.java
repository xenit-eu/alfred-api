package eu.xenit.alfred.api.dictionary.namespaces;

import java.util.Map;

public class Namespaces {

    public Map<String, Namespace> namespaces;

    public Namespaces(Map<String, Namespace> namespaces) {
        this.namespaces = namespaces;
    }

    public Namespaces() {
    }

    public Map<String, Namespace> getNamespaces() {
        return namespaces;
    }

    public void setNamespaces(Map<String, Namespace> namespaces) {
        this.namespaces = namespaces;
    }
}
