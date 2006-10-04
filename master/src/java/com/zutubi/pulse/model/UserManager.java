package com.zutubi.pulse.model;

import org.acegisecurity.userdetails.UserDetailsService;

import java.util.List;
import java.util.Set;

/**
 * 
 *
 */
public interface UserManager extends EntityManager<User>, UserDetailsService
{
    /**
     * Adds a new user to the server.
     *
     * @param newUser the user to add
     * @param grantAdminPermissions if true, the user will be granted admin
     *                              permissions directly
     * @param useLdapAuthentication if true, the user will be authenticated
     *                              via LDAP
     */
    void addUser(User newUser, boolean grantAdminPermissions, boolean useLdapAuthentication);

    /**
     * Retrieve an instance of the user identified by the login name
     * @param login
     *
     * @return a user instance, or null if no matching user is found.
     */
    User getUser(String login);

    /**
     * Retrieve an instance of the user identified by the unique id.
     * @param id
     *
     * @return a user instance, or null if no matching user is found.
     */
    User getUser(long id);

    /**
     * Retrieve a list of users who have a login name similar to the specified name. A similar login
     * name is one that contains the specified string as a substring within the login name. For example:
     * the name 'dan' would be considered similar to 'daniel' and 'brendan'
     *
     * @param name
     *
     * @return a list of user instances, or an empty list if no users match the requirements.
     */
    List<User> getUsersWithLoginLike(String name);

    /**
     * Retrieve a list of all of the users within the system.
     *
     * @return a list of all user instances.
     */
    List<User> getAllUsers();

    /**
     * Return the number of users configured in the system.
     *
     */
    int getUserCount();

    /**
     * Returns a list of projects configured by the user to hide from their
     * dashboard.
     *
     * @param user user to get the projects for
     * @return the user's hidden projects
     */
    Set<Project> getHiddenProjects(User user);

    List<Project> getVisibleProjects(User user);

    /**
     * Returns true iff the given user has been granted the given authority,
     * either directly or by being a member of a group that has been granted
     * the authority.
     *
     * @param user      the user to test
     * @param authority the authority to test for
     * @return true iff the user has been gratned the authority
     */
    boolean hasAuthority(User user, String authority);

    void setPassword(User user, String rawPassword);

    //---( Contact point related interface )---
    void save(ContactPoint contact);
    ContactPoint getContactPoint(long id);
    void delete(ContactPoint contact);

    List<Group> getAllGroups();
    List<Group> getAdminAllProjectGroups();
    Group getGroup(long id);
    Group getGroup(String name);

    void addGroup(Group group);
    void save(Group group);
    void renameGroup(Group group, String newName);
    void delete(Group group, ProjectManager projectManager);

    List<User> getUsersNotInGroup(Group group);

    long getNextBuildNumber(User user);
    void removeReferencesToProject(Project project);
}
