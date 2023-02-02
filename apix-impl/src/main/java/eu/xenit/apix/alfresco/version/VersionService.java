package eu.xenit.apix.alfresco.version;

import eu.xenit.apix.Version;
import eu.xenit.apix.version.IVersionService;
import eu.xenit.apix.version.VersionDescription;
import org.springframework.stereotype.Service;

@Service("eu.xenit.apix.version.IVersionService")
public class VersionService implements IVersionService {

    @Override
    public VersionDescription getVersionDescription() {
        VersionDescription ret = VersionDescription.createFromVersionString(
                Version.Number,
                "XeniT Api-X java alfresco wrapper and REST interface");
        return ret;
    }
}
