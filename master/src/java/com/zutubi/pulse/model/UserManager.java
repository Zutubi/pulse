package com.zutubi.pulse.model;

import com.zutubi.pulse.prototype.config.group.GroupConfiguration;
import com.zutubi.pulse.prototype.config.user.UserConfiguration;
import org.acegisecurity.userdetails.UserDetailsService;

import java.util.List;
import java.util.Set;

/**
 * 
 *
 */
public interface UserManager extends EntityManager<User>, UserDetailsService
{
    UserConfiguration getUserConfig(String login);

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

    Set<Project> getUserProjects(User user, ProjectManager projectManager);

    AcegiUser getPrinciple(User user);

    void setPassword(UserConfiguration user, String rawPassword);

    long getNextBuildNumber(User user);

    GroupConfiguration getGroupConfig(String name);
}
