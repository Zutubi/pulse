package com.zutubi.pulse.model.persistence.hibernate;

import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.RunExecutablePostBuildAction;
import com.zutubi.pulse.model.TagPostBuildAction;
import com.zutubi.pulse.model.persistence.ProjectDao;

/**
 * 
 *
 */
public class HibernateProjectDao extends HibernateEntityDao<Project> implements ProjectDao
{
    public Class persistentClass()
    {
        return Project.class;
    }

    public void save(TagPostBuildAction action)
    {
        getHibernateTemplate().saveOrUpdate(action);
    }

    public void save(RunExecutablePostBuildAction action)
    {
        getHibernateTemplate().saveOrUpdate(action);
    }

    public TagPostBuildAction findTagPostBuildAction(long id)
    {
        return (TagPostBuildAction) getHibernateTemplate().load(TagPostBuildAction.class, id);
    }

    public RunExecutablePostBuildAction findRunExecutablePostBuildAction(long id)
    {
        return  (RunExecutablePostBuildAction) getHibernateTemplate().load(RunExecutablePostBuildAction.class, id);
    }
}
