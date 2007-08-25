package com.zutubi.pulse.model.persistence.hibernate;

import com.zutubi.pulse.model.BuildHostRequirements;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.RunExecutablePostBuildAction;
import com.zutubi.pulse.model.TagPostBuildAction;
import com.zutubi.pulse.model.persistence.ProjectDao;
import com.zutubi.util.logging.Logger;

import java.util.List;

/**
 * 
 *
 */
public class HibernateProjectDao extends HibernateEntityDao<Project> implements ProjectDao
{
    private static final Logger LOG = Logger.getLogger(HibernateEntityDao.class);

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

    public List<Project> findByAdminAuthority(String recipient)
    {
        return findByNamedQuery("findByAcl", "recipient", recipient);
    }

    public void delete(BuildHostRequirements hostRequirements)
    {
        getHibernateTemplate().delete(hostRequirements);
    }
}
