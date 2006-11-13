package com.zutubi.pulse.model.persistence.mock;

import com.zutubi.pulse.model.Group;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.ProjectGroup;
import com.zutubi.pulse.model.User;
import com.zutubi.pulse.model.persistence.UserDao;

import java.util.List;
import java.util.Set;

/**
 */
public class MockUserDao extends MockEntityDao<User> implements UserDao
{
    public User findByLogin(final String login)
    {
        return findUniqueByFilter(new Filter<User>()
        {

            public boolean include(User user)
            {
                return user.getLogin().equals(login);
            }
        });
    }

    public List<User> findByLikeLogin(String login)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public int getUserCount()
    {
        return findAll().size();
    }

    public Set<Project> getShownProjects(User user)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public List<User> findByNotInGroup(Group group)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public List<User> findByShownProject(Project project)
    {
        throw new RuntimeException("Method not implemented.");
    }

    public List<User> findByShownProjectGroup(ProjectGroup group)
    {
        throw new RuntimeException("Method not yet implemented.");
    }

    public List<Project> findVisibleProjectsByUser(User user)
    {
        throw new RuntimeException("Method not implemented.");
    }
}
