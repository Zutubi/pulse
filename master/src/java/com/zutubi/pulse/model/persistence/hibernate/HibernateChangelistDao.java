package com.zutubi.pulse.model.persistence.hibernate;

import com.zutubi.pulse.core.model.Changelist;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.User;
import com.zutubi.pulse.model.persistence.ChangelistDao;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.orm.hibernate3.HibernateTemplate;

import java.util.*;

/**
 * Hibernate implementation of ChangelistDao.
 */
@SuppressWarnings({"unchecked"})
public class HibernateChangelistDao extends HibernateEntityDao<Changelist> implements ChangelistDao
{
    public Class persistentClass()
    {
        return Changelist.class;
    }

    public Set<Long> getAllAffectedProjectIds(Changelist changelist)
    {
        List<Changelist> all = findByRevision(changelist.getServerUid(), changelist.getRevision());
        Set<Long> ids = new HashSet<Long>();
        for(Changelist cl: all)
        {
            ids.add(cl.getProjectId());
        }

        return ids;
    }

    public Set<Long> getAllAffectedResultIds(Changelist changelist)
    {
        List<Changelist> all = findByRevision(changelist.getServerUid(), changelist.getRevision());
        Set<Long> ids = new HashSet<Long>();
        for(Changelist cl: all)
        {
            ids.add(cl.getResultId());
        }

        return ids;
    }

    public List<Changelist> findLatestByUser(final User user, final int max)
    {
        final List<String> allLogins = new LinkedList<String>();
        allLogins.add(user.getLogin());
        allLogins.addAll(user.getAliases());

        return findUnique(new ChangelistQuery()
        {
            public Query createQuery(Session session)
            {
                Query queryObject = session.createQuery("from Changelist model where model.revision.author in (:logins) order by model.revision.time desc");
                queryObject.setParameterList("logins", allLogins);
                return queryObject;
            }
        }, max);
    }

    public List<Changelist> findLatestByProject(final Project project, final int max)
    {
        return findUnique(new ChangelistQuery()
        {
            public Query createQuery(Session session)
            {
                Query queryObject = session.createQuery("from Changelist model where model.projectId = :projectId order by model.revision.time desc");
                queryObject.setParameter("projectId", project.getId());
                return queryObject;
            }
        }, max);
    }

    public List<Changelist> findLatestByProjects(Project[] projects, final int max)
    {
        final Long[] projectIds = new Long[projects.length];
        for(int i = 0; i < projects.length; i++)
        {
            projectIds[i] = projects[i].getId();
        }

        return findUnique(new ChangelistQuery()
        {
            public Query createQuery(Session session)
            {
                Query queryObject = session.createQuery("from Changelist model where model.projectId in (:projectIds) order by model.revision.time desc");
                queryObject.setParameterList("projectIds", projectIds);
                return queryObject;
            }
        }, max);
    }

    public List<Changelist> findByRevision(final String serverUid, final Revision revision)
    {
        return (List<Changelist>) getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException
            {
                Query queryObject = session.createQuery("from Changelist model where model.serverUid = :serverUid and model.revision.revisionString = :revisionString");
                queryObject.setParameter("serverUid", serverUid);
                queryObject.setParameter("revisionString", revision.getRevisionString());

                SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());

                return queryObject.list();
            }
        });
    }

    public List<Changelist> findByResult(final long id)
    {
        return  (List<Changelist>) getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException
            {
                Query queryObject = session.createQuery("from Changelist model where model.resultId = :resultId order by model.revision.time desc");
                queryObject.setParameter("resultId", id);

                SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());

                return queryObject.list();
            }
        });
    }

    private static interface ChangelistQuery
    {
        Query createQuery(Session session);
    }

    private List<Changelist> findUnique(final ChangelistQuery changelistQuery, final int max)
    {
        List<Changelist> results = new ArrayList<Changelist>(max);
        final int[] offset = { 0 };

        while(results.size() < max)
        {
            List<Changelist> changelists = (List<Changelist>) getHibernateTemplate().execute(new HibernateCallback()
            {
                public Object doInHibernate(Session session) throws HibernateException
                {
                    Query queryObject = changelistQuery.createQuery(session);
                    queryObject.setFirstResult(offset[0]);
                    queryObject.setMaxResults(max);
                    SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());
                    return queryObject.list();
                }
            });

            if(changelists.size() == 0)
            {
                break;
            }

            addUnique(results, changelists);
            offset[0] = offset[0] + max;
        }

        // We can actually get up to max - 1 extra entries if multiple queries
        // were run, so discard any after max (better than shrinking query max
        // as we go as then we may execute many queries).
        while(results.size() > max)
        {
            results.remove(results.size() - 1);
        }
        return results;
    }

    private void addUnique(Collection<Changelist> lists, Collection<Changelist> toAdd)
    {
        for(Changelist candidate: toAdd)
        {
            boolean found = false;
            for(Changelist existing: lists)
            {
                if(candidate.getServerUid().equals(existing.getServerUid()) && candidate.getRevision().getRevisionString().equals(existing.getRevision().getRevisionString()))
                {
                    found = true;
                    break;
                }
            }

            if(!found)
            {
                lists.add(candidate);
            }
        }
    }
}
