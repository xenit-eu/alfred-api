package eu.xenit.alfred.api.people;

import eu.xenit.alfred.api.data.NodeRef;
import eu.xenit.alfred.api.groups.Group;

import java.util.List;
import java.util.Set;

/**
 * Service around people and groups.
 */
public interface IPeopleService {

    /**
     * Gets the information of a person represented by a noderef.
     *
     * @param ref The noderef that represents a person.
     * @return The information of a person.
     */
    Person GetPerson(NodeRef ref);

    /**
     * Gets the subgroups of the given group (i.e. the contained authorities of type group)
     *
     * @param name Name of the parent group of which you want to find the subgroups
     * @param immediate Set to true if you only want direct subgroups, not subgroups-of-subgroups and beyond
     * @return null, if the given group does not exist
     */
    List<Group> GetSubgroupsInGroup(String name, boolean immediate);

    /**
     * Gets the information of a person represented by a userName.
     *
     * @param userName The user name that represents a person.
     * @return The information of a person.
     */
    Person GetPerson(String userName);

    /**
     * Gets the information of all the people existing in alfresco.
     *
     * @return The information of all the people.
     */
    List<Person> GetPeople();

    /**
     * Gets the information of all the people of a given group.
     *
     * @param immediate If true, only look at the direct member of this group. If false, look at the members of all
     * recursive child groups.
     * @param name The name of the group.
     * @return The information of all the people.
     */
    List<Person> GetUsersOfGroup(String name, boolean immediate);

    /**
     * Gets the information of a group, identified by the groupIdentifier (this is the name with the GROUP_ prefix)
     *
     * @param groupIdentifier the identifying name unique to the group
     * @return A container object with the name, identifier and noderef of the group
     */
    Group GetGroup(String groupIdentifier);

    /**
     * Gets the information of a group, identified by the nodeRef in which the group is stored
     *
     * @param nodeRef the nodeRef in which the group is stored
     * @return A container object with the name, identifier and noderef of the group
     */
    Group GetGroup(NodeRef nodeRef);

    /**
     * Gets a list of all groups
     *
     * @return A list of Group objects, one for each group in this Alfresco
     */
    List<Group> GetGroups();

    /**
     * Unlink an authority (user or subgroup) from a parent group.
     *
     * @param parentgroup The parent group that will lose childauthority as a child
     * @param childauthority The child authority (user or group) that will lose parentgroup as a parent
     */
    void UnlinkFromParentGroup(String parentgroup, String childauthority);

    /**
     * Adds an authority as a child to a group. Users become members of the parent group, groups become subgroups of the
     * parent group (so all of their users will be member of the parent group too)
     *
     * @param parentgroup The group that will gain a child
     * @param childauthority The authority that will gain a parent to be member/subgroup of
     */
    void AddToParentGroup(String parentgroup, String childauthority);

    /**
     * @return The current user its information.
     */
    Person GetCurrentUser();

    /**
     * Gets the groups of the current user
     *
     * @param userName The name of the user of which the groups are requested.
     * @return A list of the names of the groups of the given user.
     */
    Set<String> GetContainerGroups(String userName);

    /***
     *  Checks if authority with that name is user
     * @param authorityName The name of the authority to check
     * @return Whether the authority is a user
     */
    boolean isUser(String authorityName);

    /***
     *  Checks if authority with that name is of type group
     * @param authorityName The name of the authority to check
     * @return Whether the authority is a group
     */
    boolean isGroup(String authorityName);
}
