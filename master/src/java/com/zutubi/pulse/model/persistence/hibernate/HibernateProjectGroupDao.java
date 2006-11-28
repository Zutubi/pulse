package com.zutubi.pulse.model.persistence.hibernate;

import com.zutubi.pulse.model.ProjectGroup;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.persistence.ProjectGroupDao;

import java.util.List;

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

    public List<ProjectGroup> findByProject(Project project)
    {
        return findByNamedQuery("findProjectGroupByProject", "project", project);
    }
}
