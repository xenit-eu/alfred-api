package eu.xenit.apix.alfresco.version;

import com.github.dynamicextensionsalfresco.osgi.OsgiService;
import eu.xenit.apix.Version;
import eu.xenit.apix.version.IVersionService;
import eu.xenit.apix.version.VersionDescription;
import org.springframework.stereotype.Component;

@OsgiService
@Component("eu.xenit.apix.version.IVersionService")
public class VersionService implements IVersionService {

    @Override
    public VersionDescription getVersionDescription() {
        VersionDescription ret = VersionDescription.createFromVersionString(
                Version.Number,
                "XeniT Api-X java alfresco wrapper and REST interface");
        return ret;
    }
}
