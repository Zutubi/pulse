package com.cinnamonbob.model.persistence.hibernate;

import com.cinnamonbob.model.AntBobFileDetails;
import com.cinnamonbob.model.CustomBobFileDetails;
import com.cinnamonbob.model.MakeBobFileDetails;
import com.cinnamonbob.model.Project;
import com.cinnamonbob.model.persistence.ProjectDao;
import com.cinnamonbob.util.logging.Logger;

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

    public Project findByName(final String name)
    {
        return (Project) findUniqueByNamedQuery("findByName", "name", name, true);
    }

    public List<Project> findByLikeName(final String name)
    {
        return findByNamedQuery("findByLikeName", "name", name);
    }

    public void save(CustomBobFileDetails details)
    {
        getHibernateTemplate().saveOrUpdate(details);
    }

    public CustomBobFileDetails findCustomBobFileSource(long id)
    {
        return (CustomBobFileDetails) getHibernateTemplate().load(CustomBobFileDetails.class, id);
    }

    public void save(AntBobFileDetails source)
    {
        getHibernateTemplate().saveOrUpdate(source);
    }

    public void save(MakeBobFileDetails source)
    {
        getHibernateTemplate().saveOrUpdate(source);
    }

    public AntBobFileDetails findAntBobFileSource(long id)
    {
        return (AntBobFileDetails) getHibernateTemplate().load(AntBobFileDetails.class, id);
    }

    public MakeBobFileDetails findMakeBobFileSource(long id)
    {
        return (MakeBobFileDetails) getHibernateTemplate().load(MakeBobFileDetails.class, id);
    }
}