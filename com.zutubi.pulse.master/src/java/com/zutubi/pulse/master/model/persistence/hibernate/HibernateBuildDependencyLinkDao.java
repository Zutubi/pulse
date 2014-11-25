package com.zutubi.pulse.master.model.persistence.hibernate;

import com.zutubi.pulse.master.model.BuildDependencyLink;
import com.zutubi.pulse.master.model.persistence.BuildDependencyLinkDao;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate4.HibernateCallback;

import java.sql.SQLException;
import java.util.List;

/**
 * Hibernate-based implementation of {@link BuildDependencyLinkDao}.
 */
@SuppressWarnings("unchecked")
public class HibernateBuildDependencyLinkDao extends HibernateEntityDao<BuildDependencyLink> implements BuildDependencyLinkDao
{
    @Override
    public Class<BuildDependencyLink> persistentClass()
    {
        return BuildDependencyLink.class;
    }

    public List<BuildDependencyLink> findAllDependencies(final long buildId)
    {
        return getHibernateTemplate().execute(new HibernateCallback<List<BuildDependencyLink>>()
        {
            public List<BuildDependencyLink> doInHibernate(Session session) throws HibernateException
            {
                Query query = session.createQuery("from BuildDependencyLink where downstreamBuildId = :build or upstreamBuildId = :build");
                query.setLong("build", buildId);
                return query.list();
            }
        });
    }

    public List<BuildDependencyLink> findAllUpstreamDependencies(final long buildId)
    {
        return getHibernateTemplate().execute(new HibernateCallback<List<BuildDependencyLink>>()
        {
            public List<BuildDependencyLink> doInHibernate(Session session) throws HibernateException
            {
                Query query = session.createQuery("from BuildDependencyLink where downstreamBuildId = :build");
                query.setLong("build", buildId);
                return query.list();
            }
        });
    }

    public List<BuildDependencyLink> findAllDownstreamDependencies(final long buildId)
    {
        return getHibernateTemplate().execute(new HibernateCallback<List<BuildDependencyLink>>()
        {
            public List<BuildDependencyLink> doInHibernate(Session session) throws HibernateException
            {
                Query query = session.createQuery("from BuildDependencyLink where upstreamBuildId = :build");
                query.setLong("build", buildId);
                return query.list();
            }
        });
    }

    public int deleteDependenciesByBuild(final long buildId)
    {
        return getHibernateTemplate().execute(new HibernateCallback<Integer>()
        {
            public Integer doInHibernate(Session session) throws HibernateException
            {
                Query queryObject = session.createQuery("delete from BuildDependencyLink where upstreamBuildId = :build or downstreamBuildId = :build");
                queryObject.setLong("build", buildId);
                return queryObject.executeUpdate();
            }
        });
    }
}
