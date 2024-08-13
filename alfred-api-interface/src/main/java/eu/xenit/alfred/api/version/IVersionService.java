package eu.xenit.alfred.api.version;

/**
 * Versioning information regarding Alfred API itself
 */
public interface IVersionService {

    /**
     * @return The current version of Alfred API.
     */
    VersionDescription getVersionDescription();
}
