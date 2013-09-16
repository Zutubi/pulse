package com.zutubi.pulse.master.model.persistence.hibernate;

import com.zutubi.pulse.core.model.PersistentChangelist;
import com.zutubi.pulse.core.model.PersistentFileChange;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.model.persistence.ChangelistDao;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.SessionFactoryUtils;

import java.sql.SQLException;
import java.util.*;

/**
 * Hibernate implementation of {@link ChangelistDao}.
 */
@SuppressWarnings({"unchecked"})
public class HibernateChangelistDao extends HibernateEntityDao<PersistentChangelist> implements ChangelistDao
{
    public Class<PersistentChangelist> persistentClass()
    {
        return PersistentChangelist.class;
    }

    public Set<Long> findAllAffectedProjectIds(PersistentChangelist changelist)
    {
        List<PersistentChangelist> all = findAllEquivalent(changelist);
        Set<Long> ids = new HashSet<Long>();
        for(PersistentChangelist cl: all)
        {
            ids.add(cl.getProjectId());
        }

        return ids;
    }

    public Set<Long> findAllAffectedResultIds(PersistentChangelist changelist)
    {
        List<PersistentChangelist> all = findAllEquivalent(changelist);
        Set<Long> ids = new HashSet<Long>();
        for(PersistentChangelist cl: all)
        {
            ids.add(cl.getResultId());
        }

        return ids;
    }

    public List<PersistentChangelist> findLatestByUser(final User user, final int max)
    {
        final List<String> allLogins = new LinkedList<String>();
        allLogins.add(user.getLogin());
        allLogins.addAll(user.getPreferences().getAliases());

        return findUnique(new ChangelistQuery()
        {
            public Query createQuery(Session session)
            {
                Query queryObject = session.createQuery("from PersistentChangelist model where model.author in (:logins) order by model.time desc, model.id desc");
                queryObject.setParameterList("logins", allLogins);
                return queryObject;
            }
        }, max);
    }

    public List<PersistentChangelist> findLatestByProject(final Project project, final int max)
    {
        return findUnique(new ChangelistQuery()
        {
            public Query createQuery(Session session)
            {
                Query queryObject = session.createQuery("from PersistentChangelist model where model.projectId = :projectId order by model.time desc, model.id desc");
                queryObject.setParameter("projectId", project.getId());
                return queryObject;
            }
        }, max);
    }

    public List<PersistentChangelist> findLatestByProjects(Project[] projects, final int max)
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
                Query queryObject = session.createQuery("from PersistentChangelist model where model.projectId in (:projectIds) order by model.time desc, model.id desc");
                queryObject.setParameterList("projectIds", projectIds);
                return queryObject;
            }
        }, max);
    }

    public List<PersistentChangelist> findAllEquivalent(final PersistentChangelist changelist)
    {
        List<PersistentChangelist> result = getHibernateTemplate().execute(new HibernateCallback<List<PersistentChangelist>>()
        {
            public List<PersistentChangelist> doInHibernate(Session session) throws HibernateException
            {
                Query queryObject = session.createQuery("from PersistentChangelist model where model.hash = :hash");
                queryObject.setParameter("hash", changelist.getHash());

                SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());

                return queryObject.list();
            }
        });

        // Now eliminate false-positives from hash collisions.
        for (Iterator<PersistentChangelist> it = result.iterator(); it.hasNext(); )
        {
            PersistentChangelist current = it.next();
            if (!current.isEquivalent(changelist))
            {
                it.remove();
            }
        }

        return result;
    }

    public int getSize(final PersistentChangelist changelist)
    {
        return toInt(getHibernateTemplate().execute(new HibernateCallback<Long>()
        {
            public Long doInHibernate(Session session) throws HibernateException, SQLException
            {
                Query queryObject = session.createQuery("select count(change) from PersistentChangelist model join model.changes as change where model = :changelist");
                queryObject.setEntity("changelist", changelist);
                SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());
                return (Long) queryObject.uniqueResult();
            }
        }));
    }

    public List<PersistentFileChange> getFiles(final PersistentChangelist changelist, final int offset, final int max)
    {
        return getHibernateTemplate().execute(new HibernateCallback<List<PersistentFileChange>>()
        {
            public List<PersistentFileChange> doInHibernate(Session session) throws HibernateException, SQLException
            {
                Query queryObject = session.createFilter(changelist.getChanges(), "order by ordinal");
                queryObject.setFirstResult(offset);
                queryObject.setMaxResults(max);
                SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());
                return queryObject.list();
            }
        });
    }

    public List<PersistentChangelist> findByResult(final long id, final boolean allowEmpty)
    {
        final String queryString;
        if (allowEmpty)
        {
            queryString = "from PersistentChangelist model where model.resultId = :resultId order by model.time desc, model.id desc";
        }
        else
        {
            queryString = "from PersistentChangelist model where model.resultId = :resultId and size(model.changes) > 0 order by model.time desc, model.id desc";
        }
        
        return getHibernateTemplate().execute(new HibernateCallback<List<PersistentChangelist>>()
        {
            public List<PersistentChangelist> doInHibernate(Session session) throws HibernateException
            {
                Query queryObject = session.createQuery(queryString);
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

    private List<PersistentChangelist> findUnique(final ChangelistQuery changelistQuery, final int max)
    {
        List<PersistentChangelist> results = new ArrayList<PersistentChangelist>(max);
        final int[] offset = { 0 };
        final int[] limit = { max };
        int remaining = max;

        while (remaining > 0)
        {
            List<PersistentChangelist> changelists = getHibernateTemplate().execute(new HibernateCallback<List<PersistentChangelist>>()
            {
                public List<PersistentChangelist> doInHibernate(Session session) throws HibernateException
                {
                    Query queryObject = changelistQuery.createQuery(session);
                    queryObject.setFirstResult(offset[0]);
                    queryObject.setMaxResults(limit[0]);
                    SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());
                    return queryObject.list();
                }
            });

            if (changelists.size() == 0)
            {
                break;
            }

            int added = addUnique(results, changelists);
            offset[0] = offset[0] + limit[0];

            if (added < remaining / 2)
            {
                // We got less than half of what we needed from that query, ramp up the limit to
                // avoid doing heaps of queries (CIB-3066).
                limit[0] *= 2;
            }

            remaining -= added;
        }

        // We can get extra entries if multiple queries were run, so discard any after max.  (Note
        // we don't shrink the query limit as we go - in fact we may even increase it - because we
        // want to avoid doing many queries.)
        while(results.size() > max)
        {
            results.remove(results.size() - 1);
        }
        return results;
    }

    private int addUnique(Collection<PersistentChangelist> lists, Collection<PersistentChangelist> toAdd)
    {
        int added = 0;
        for (PersistentChangelist candidate: toAdd)
        {
            boolean found = false;
            for (PersistentChangelist existing: lists)
            {
                if (existing.isEquivalent(candidate))
                {
                    found = true;
                    break;
                }
            }

            if (!found)
            {
                lists.add(candidate);
                added++;
            }
        }

        return added;
    }
}
