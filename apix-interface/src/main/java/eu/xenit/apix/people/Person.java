package eu.xenit.apix.people;

import eu.xenit.apix.data.NodeRef;

import java.util.List;
import java.io.Serializable;
import java.util.Objects;

/**
 * Datastructure that represents a person in alfresco and its information. nodeRef: The noderef of a person. userName:
 * The unique id of the user expressed as a string. firstName: The first name of the user. lastName: The last name of
 * the user. emailAddress: The email address of the user. groups: The names of the groups that a user is in.
 */
public class Person implements Serializable {

    private static final long serialVersionUID = 3979634213050121462L;

    private NodeRef nodeRef;
    private String userName;
    private String firstName;
    private String lastName;
    private String emailAddress;
    private List<String> groups;

    public Person() {
    }

    public Person(NodeRef nodeRef, String userName, String firstName, String lastName, String emailAddress,
            List<String> groups) {
        this.userName = userName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.nodeRef = nodeRef;
        this.emailAddress = emailAddress;
        this.groups = groups;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public List<String> getGroups() {
        return groups;
    }

    public void setGroups(List<String> groups) {
        this.groups = groups;
    }

    public NodeRef getNodeRef() {
        return nodeRef;
    }

    public void setNodeRef(NodeRef nodeRef) {
        this.nodeRef = nodeRef;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        if (!super.equals(object)) {
            return false;
        }

        Person person = (Person) object;

        if (!Objects.equals(nodeRef, person.nodeRef)) {
            return false;
        }
        if (!Objects.equals(userName, person.userName)) {
            return false;
        }
        if (!Objects.equals(firstName, person.firstName)) {
            return false;
        }
        if (!Objects.equals(lastName, person.lastName)) {
            return false;
        }
        if (!Objects.equals(emailAddress, person.emailAddress)) {
            return false;
        }
        return Objects.equals(groups, person.groups);
    }

    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (nodeRef != null ? nodeRef.hashCode() : 0);
        result = 31 * result + (userName != null ? userName.hashCode() : 0);
        result = 31 * result + (firstName != null ? firstName.hashCode() : 0);
        result = 31 * result + (lastName != null ? lastName.hashCode() : 0);
        result = 31 * result + (emailAddress != null ? emailAddress.hashCode() : 0);
        result = 31 * result + (groups != null ? groups.hashCode() : 0);
        return result;
    }
}
