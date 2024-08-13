package eu.xenit.alfred.api.alfresco.version;

import eu.xenit.alfred.api.Version;
import eu.xenit.alfred.api.version.IVersionService;
import eu.xenit.alfred.api.version.VersionDescription;
import org.springframework.stereotype.Service;

@Service("eu.xenit.alfred.api.version.IVersionService")
public class VersionService implements IVersionService {

    @Override
    public VersionDescription getVersionDescription() {
        VersionDescription ret = VersionDescription.createFromVersionString(
                Version.Number,
                "Xenit Alfred API Alfresco wrapper and REST interface");
        return ret;
    }
}
