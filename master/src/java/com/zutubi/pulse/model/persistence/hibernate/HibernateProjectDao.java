/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.model.persistence.hibernate;

import com.zutubi.pulse.model.*;
import com.zutubi.pulse.model.persistence.ProjectDao;
import com.zutubi.pulse.util.logging.Logger;

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

    public void save(VersionedPulseFileDetails details)
    {
        getHibernateTemplate().saveOrUpdate(details);
    }

    public VersionedPulseFileDetails findVersionedPulseFileDetails(long id)
    {
        return (VersionedPulseFileDetails) getHibernateTemplate().load(VersionedPulseFileDetails.class, id);
    }

    public void save(AntPulseFileDetails source)
    {
        getHibernateTemplate().saveOrUpdate(source);
    }

    public void save(MakePulseFileDetails source)
    {
        getHibernateTemplate().saveOrUpdate(source);
    }

    public AntPulseFileDetails findAntPulseFileSource(long id)
    {
        return (AntPulseFileDetails) getHibernateTemplate().load(AntPulseFileDetails.class, id);
    }

    public MakePulseFileDetails findMakePulseFileSource(long id)
    {
        return (MakePulseFileDetails) getHibernateTemplate().load(MakePulseFileDetails.class, id);
    }
}