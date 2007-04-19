package com.zutubi.pulse.model.persistence.hibernate;

import com.zutubi.pulse.model.*;
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

    public Project findByName(final String name)
    {
        return (Project) findUniqueByNamedQuery("findByName", "name", name, true);
    }

    public List<Project> findByLikeName(final String name)
    {
        return findByNamedQuery("findByLikeName", "name", name);
    }

    public Project findByScm(Scm scm)
    {
        return (Project) findUniqueByNamedQuery("findByScm", "scm", scm);
    }

    public Project findByScmId(long id)
    {
        return (Project) findUniqueByNamedQuery("findByScmId", "scmId", id);
    }

    public Project findByBuildSpecification(BuildSpecification buildSpecification)
    {
        return (Project) findUniqueByNamedQuery("findProjectByBuildSpecification", "buildSpecification", buildSpecification);
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

    public void save(TagPostBuildAction action)
    {
        getHibernateTemplate().saveOrUpdate(action);
    }

    public void save(RunExecutablePostBuildAction action)
    {
        getHibernateTemplate().saveOrUpdate(action);
    }

    public AntPulseFileDetails findAntPulseFileSource(long id)
    {
        return (AntPulseFileDetails) getHibernateTemplate().load(AntPulseFileDetails.class, id);
    }

    public MakePulseFileDetails findMakePulseFileSource(long id)
    {
        return (MakePulseFileDetails) getHibernateTemplate().load(MakePulseFileDetails.class, id);
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

    public Project findByCleanupRule(CleanupRule rule)
    {
        return (Project) findUniqueByNamedQuery("findProjectByCleanupRule", "cleanupRule", rule);
    }

    public List<Project> findAllProjectsCached()
    {
        return findByNamedQuery("findAllProjectsCached", 0, true);
    }
}
