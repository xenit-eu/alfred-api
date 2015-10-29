package eu.xenit.apix.properties;

/**
 * Determines how the property its value is tokenized in the search index. If 'true', the string value of the property
 * is tokenized before indexing if 'false', it is indexed 'as is' as a single string if 'both' then both forms above are
 * in the index
 */
public enum PropertyTokenised {
    TRUE, FALSE, BOTH
}