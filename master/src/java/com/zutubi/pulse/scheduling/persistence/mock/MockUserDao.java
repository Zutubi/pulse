/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.scheduling.persistence.mock;

import com.zutubi.pulse.model.User;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.persistence.UserDao;

import java.util.List;

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

    public List<Project> getProjects(User user)
    {
        throw new RuntimeException("Method not implemented.");
    }
}
