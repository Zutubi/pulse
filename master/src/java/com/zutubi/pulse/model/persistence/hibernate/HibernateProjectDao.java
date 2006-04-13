/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.model.persistence.hibernate;

import com.zutubi.pulse.model.AntPulseFileDetails;
import com.zutubi.pulse.model.CustomPulseFileDetails;
import com.zutubi.pulse.model.MakePulseFileDetails;
import com.zutubi.pulse.model.Project;
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

    public void save(CustomPulseFileDetails details)
    {
        getHibernateTemplate().saveOrUpdate(details);
    }

    public CustomPulseFileDetails findCustomPulseFileSource(long id)
    {
        return (CustomPulseFileDetails) getHibernateTemplate().load(CustomPulseFileDetails.class, id);
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