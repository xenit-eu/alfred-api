package eu.xenit.alfred.api.dictionary.namespaces;

import java.util.List;

public class Namespace {

    public String URI;
    public List<String> prefixes;

    public Namespace(String URI, List<String> prefixes) {
        this.URI = URI;
        this.prefixes = prefixes;
    }

    public Namespace() {
    }
}
