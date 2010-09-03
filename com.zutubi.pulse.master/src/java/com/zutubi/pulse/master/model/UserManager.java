package com.zutubi.pulse.master.model;

import com.zutubi.pulse.master.security.AcegiUser;
import com.zutubi.pulse.master.tove.config.group.GroupConfiguration;
import com.zutubi.pulse.master.tove.config.group.UserGroupConfiguration;
import com.zutubi.pulse.master.tove.config.user.UserConfiguration;
import org.springframework.security.userdetails.UserDetailsService;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * 
 *
 */
public interface UserManager extends EntityManager<User>, UserDetailsService
{
    String ALL_USERS_GROUP_NAME       = "all users";
    String ANONYMOUS_USERS_GROUP_NAME = "anonymous users";
    String PROJECT_ADMINS_GROUP_NAME  = "project administrators";
    String ADMINS_GROUP_NAME          = "administrators";
    String DEVELOPERS_GROUP_NAME      = "developers";

    UserConfiguration getUserConfig(String login);

    /**
     * Retrieve an instance of the user identified by the login name
     *
     * @param login login of the user to find
     * @return a user instance, or null if no matching user is found.
     */
    User getUser(String login);

    /**
     * Retrieve an instance of the user identified by the unique id.
     * 
     * @param id id of the user to find
     * @return a user instance, or null if no matching user is found.
     */
    User getUser(long id);

    /**
     * Retrieve a list of all of the users within the system.
     *
     * @return a list of all user instances.
     */
    List<User> getAllUsers();

    /**
     * @return the number of users configured in the system.
     */
    int getUserCount();

    Set<Project> getUserProjects(User user, ProjectManager projectManager);

    AcegiUser getPrinciple(UserConfiguration user);
    
    AcegiUser getPrinciple(User user);

    /**
     * Checks if the given password matches the current password of the given
     * user.
     *  
     * @param user     user to check the password against
     * @param password the password to check
     * @return true if the given password matches the given user's password
     */
    boolean checkPassword(UserConfiguration user, String password);
    
    void setPassword(UserConfiguration user, String rawPassword);

    long updateAndGetNextBuildNumber(User user);

    UserGroupConfiguration getGroupConfig(String name);

    /**
     * Returns a collection of all users that are members of a given group.
     * Handles built-in groups for which the members are not explicitly
     * configured.
     *
     * @param group group to get the members of
     * @return a collection of all users that are members of the group
     */
    Collection<UserConfiguration> getGroupMembers(GroupConfiguration group);

    /**
     * Insert a new user.
     *
     * @param user configuration to be inserted.
     *
     * @return the persistent version of the user parameter.
     */
    UserConfiguration insert(UserConfiguration user);

    /**
     * Clean the responsibilities for the specified user.
     *
     * @param user  whose responsibilities will be cleared.
     */
    void clearAllResponsibilities(User user);
}
