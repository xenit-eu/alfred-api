package eu.xenit.alfred.api.alfresco.people;

import eu.xenit.alfred.api.alfresco.ApixToAlfrescoConversion;
import eu.xenit.alfred.api.data.NodeRef;
import eu.xenit.alfred.api.groups.Group;
import eu.xenit.alfred.api.people.IPeopleService;
import eu.xenit.alfred.api.people.Person;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service("eu.xenit.alfred.api.people.IPeopleService")
@Primary
public class PeopleService implements IPeopleService {

    private static final Logger logger = LoggerFactory.getLogger(PeopleService.class);

    private ApixToAlfrescoConversion c;
    private PersonService alfrescoPersonService;
    private NodeService nodeService;
    private AuthorityService authorityService;
    private ServiceRegistry serviceRegistry;

    @Autowired
    public PeopleService(PersonService personService, ApixToAlfrescoConversion apixToAlfrescoConversion,
            NodeService nodeService, ServiceRegistry serviceRegistry) {
        this.alfrescoPersonService = personService;
        this.nodeService = nodeService;
        this.c = apixToAlfrescoConversion;
        this.authorityService = serviceRegistry.getAuthorityService();
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    public Person GetPerson(NodeRef nodeRef) throws IllegalArgumentException, NoSuchElementException {
        if (nodeRef == null) {
            throw new IllegalArgumentException("NodeRef cannot be null");
        }
        org.alfresco.service.cmr.repository.NodeRef alfrescoNodeRef = c.alfresco(nodeRef);
        if (!nodeService.exists(alfrescoNodeRef)) {
            throw new NoSuchElementException("User with NodeRef=" + alfrescoNodeRef.toString() + " does not exist");
        }
        PersonService.PersonInfo info = alfrescoPersonService.getPerson(alfrescoNodeRef);
        String username = info.getUserName();
        Set<String> groupsSet = authorityService.getContainingAuthorities(AuthorityType.GROUP, username, false);
        List<String> groups = new ArrayList<String>(groupsSet);
        String emailAddress = (String) nodeService.getProperty(info.getNodeRef(), ContentModel.PROP_EMAIL);
        NodeRef ref = c.apix(info.getNodeRef());
        return new Person(ref, info.getUserName(), info.getFirstName(), info.getLastName(), emailAddress, groups);
    }

    @Override
    public List<Person> GetPeople() {
        List<Person> ret = new ArrayList<Person>();
        PagingResults<PersonService.PersonInfo> pageResults = alfrescoPersonService.getPeople(
                "",
                new ArrayList<org.alfresco.service.namespace.QName>(),
                new ArrayList<Pair<org.alfresco.service.namespace.QName, Boolean>>(),
                new PagingRequest(Integer.MAX_VALUE));
        Set<org.alfresco.service.cmr.repository.NodeRef> people = new HashSet<>();
        for (PersonService.PersonInfo personInfo : pageResults.getPage()) {
            people.add(personInfo.getNodeRef());
        }
        for (org.alfresco.service.cmr.repository.NodeRef personNodeRef : people) {
            Person p = GetPerson(c.apix(personNodeRef));
            ret.add(p);
        }
        return ret;
    }

    @Override
    public List<Person> GetUsersOfGroup(String name, boolean immediate) {
        name = normalizeUserName(name);
        List<Person> ret = new ArrayList<Person>();
        if (!authorityService.authorityExists(name)) {
            return null;
        }
        Set<String> peopleAndGroups = authorityService.getContainedAuthorities(AuthorityType.USER, name, immediate);
        for (String person : peopleAndGroups) {
            Person p = GetPerson(person);
            ret.add(p);
        }
        return ret;
    }

    @Override
    public List<Group> GetSubgroupsInGroup(String name, boolean immediate) {
        if (!authorityService.authorityExists(name)) {
            return null;
        }
        List<Group> ret = new ArrayList<Group>();
        Set<String> peopleAndGroups = authorityService.getContainedAuthorities(AuthorityType.GROUP, name, immediate);
        for (String group : peopleAndGroups) {
            Group g = GetGroup(group);
            ret.add(g);
        }
        return ret;
    }

    private AuthorityType AuthorityTypeToAlfresco(eu.xenit.alfred.api.people.AuthorityType authorityType) {
        if (authorityType == null) {
            return null;
        }
        if (authorityType == eu.xenit.alfred.api.people.AuthorityType.EVERYONE) {
            return AuthorityType.EVERYONE;
        }
        throw new Error("Unknown authorityType");
    }

    @Override
    public Person GetPerson(String userName) throws IllegalArgumentException, NoSuchElementException {
        if (userName == null || userName.equals("")) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        String normalizedUserName = normalizeUserName(userName);
        NodeRef personRef = c.apix(alfrescoPersonService.getPersonOrNull(normalizedUserName));
        if (personRef == null) {
            throw new NoSuchElementException("User " + normalizedUserName + " does not exist");
        }
        return GetPerson(personRef);
    }

    @Override
    public Group GetGroup(String groupIdentifier) {
        String displayName = authorityService.getAuthorityDisplayName(groupIdentifier);
        NodeRef nodeRef = c.apix(authorityService.getAuthorityNodeRef(groupIdentifier));

        return new Group(nodeRef, groupIdentifier, displayName);
    }

    @Override
    public Group GetGroup(NodeRef nodeRef) {
        if (nodeRef == null) {
            return null;
        }
        String groupName = (String) nodeService.getProperty(c.alfresco(nodeRef), ContentModel.PROP_AUTHORITY_NAME);
        return GetGroup(groupName);
    }

    @Override
    public List<Group> GetGroups() {
        List<Group> groups = new ArrayList<>();

        int skipCount = 0;
        PagingResults<String> res = null;
        do {
            res = authorityService
                    .getAuthorities(AuthorityType.GROUP, null, null, false, false, new PagingRequest(skipCount, 1000));
            for (String auth : res.getPage()) {
                groups.add(GetGroup(auth));
            }
            skipCount += 1000;
        } while (res.hasMoreItems());

        return groups;
    }

    @Override
    public void UnlinkFromParentGroup(String parentgroup, String childauthority) {
        String child = normalizeUserName(childauthority);
        authorityService.removeAuthority(parentgroup, child);
    }

    @Override
    public void AddToParentGroup(String parentgroup, String childauthority) {
        String child = normalizeUserName(childauthority);
        authorityService.addAuthority(parentgroup, child);
    }

    @Override
    public Person GetCurrentUser() {
        return GetPerson(AuthenticationUtil.getRunAsUser());
    }

    @Override
    public Set<String> GetContainerGroups(String userName) {
        userName = normalizeUserName(userName);
        logger.debug("retrieving ContainerGroups of user({}).}", userName);
        return authorityService
                .getContainingAuthoritiesInZone(AuthorityType.GROUP, userName, AuthorityService.ZONE_APP_DEFAULT, null,
                        1000);
    }

    @Override
    public boolean isUser(String authorityName) {
        return isAuthorityType(authorityName, AuthorityType.USER);
    }

    @Override
    public boolean isGroup(String authorityName) {
        return isAuthorityType(authorityName, AuthorityType.GROUP);
    }

    public boolean isAuthorityType(String authorityName, AuthorityType typeToMatch) {
        AuthorityType authorityType = AuthorityType.getAuthorityType(authorityName);
        return authorityType == typeToMatch;
    }

    private String normalizeUserName(String name) {
        logger.debug("Converting " + name);
        if (name.equals("-me-")) {
            String ret = AuthenticationUtil.getFullyAuthenticatedUser();
            logger.debug("Changing to " + ret);
            return ret;
        }
        logger.debug("" + name.length());
        logger.debug("Keeping " + name);
        return name;
    }

}


