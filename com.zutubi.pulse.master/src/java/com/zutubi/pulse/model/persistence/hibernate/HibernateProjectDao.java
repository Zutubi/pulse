package com.zutubi.pulse.model.persistence.hibernate;

import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.persistence.ProjectDao;

/**
 */
public class HibernateProjectDao extends HibernateEntityDao<Project> implements ProjectDao
{
    public Class persistentClass()
    {
        return Project.class;
    }
}
