package eu.xenit.apix.search;

/**
 * Enum that represents the consistency of the search query. Can be transactional, eventual or transactional if possible.
 */
public enum SearchQueryConsistency {
    EVENTUAL,
    TRANSACTIONAL,
    TRANSACTIONAL_IF_POSSIBLE
}
