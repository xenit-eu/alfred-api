package eu.xenit.apix.sites;

import java.util.List;

public interface ISiteService {

    List<ISite> getUserSites(String userId);
}
