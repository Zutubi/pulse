package com.zutubi.pulse.model.persistence.hibernate;

import com.zutubi.pulse.model.persistence.GroupDao;
import com.zutubi.pulse.model.Group;

import java.util.List;

/**
 */
public class HibernateGroupDao extends HibernateEntityDao<Group> implements GroupDao
{
    public Class persistentClass()
    {
        return Group.class;
    }

    public Group findByName(final String name)
    {
        return (Group) findUniqueByNamedQuery("findGroupByName", "name", name, true);
    }

    public List<Group> findByAdminAllProjects()
    {
        return findByNamedQuery("findGroupByAdminAllProjects");
    }
}
