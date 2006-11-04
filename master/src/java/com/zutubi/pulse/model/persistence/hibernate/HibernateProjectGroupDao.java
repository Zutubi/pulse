package com.zutubi.pulse.model.persistence.hibernate;

import com.zutubi.pulse.model.ProjectGroup;
import com.zutubi.pulse.model.persistence.ProjectGroupDao;

/**
 */
public class HibernateProjectGroupDao extends HibernateEntityDao<ProjectGroup> implements ProjectGroupDao
{
    public Class persistentClass()
    {
        return ProjectGroup.class;
    }

    public ProjectGroup findByName(String name)
    {
        return (ProjectGroup) findUniqueByNamedQuery("findProjectGroupByName", "name", name, true);        
    }
}
