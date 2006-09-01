package com.zutubi.pulse.model.persistence.mock;

import com.zutubi.pulse.model.Group;
import com.zutubi.pulse.model.User;
import com.zutubi.pulse.model.persistence.GroupDao;

import java.util.List;

/**
 */
public class MockGroupDao extends MockEntityDao<Group> implements GroupDao
{
    public Group findByName(final String name)
    {
        return findUniqueByFilter(new Filter<Group>()
        {
            public boolean include(Group o)
            {
                return o.getName().equals(name);
            }
        });
    }

    public List<Group> findByAdminAllProjects()
    {
        return findByFilter(new Filter<Group>()
        {
            public boolean include(Group o)
            {
                return o.getAdminAllProjects() == true;
            }
        });
    }

    public List<Group> findByMember(final User member)
    {
        return findByFilter(new Filter<Group>()
        {
            public boolean include(Group o)
            {
                return o.getUsers().contains(member);
            }
        });
    }
}
