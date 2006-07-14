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
    String ANONYMOUS_LOGIN = "anonymous";

    void addUser(User newUser, boolean grantAdminPermissions);
    void addUser(User newUser, boolean grantAdminPermissions, boolean useLdapAuthencation);

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

    void setPassword(User user, String rawPassword);

    //---( Contact point related interface )---
    void save(ContactPoint contact);
    ContactPoint getContactPoint(long id);
    void delete(ContactPoint contact);
}
