package eu.xenit.alfred.api.sites;

import java.util.List;

public interface ISiteService {

    List<ISite> getUserSites(String userId);
}
