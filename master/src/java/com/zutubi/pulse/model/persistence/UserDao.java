package com.zutubi.pulse.model.persistence;

import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.User;

import java.util.List;
import java.util.Set;

/**
 * 
 *
 */
public interface UserDao extends EntityDao<User>
{
    User findByLogin(String login);

    List<User> findByLikeLogin(String login);

    Set<Project> getHiddenProjects(User user);
}
