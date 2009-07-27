package com.zutubi.pulse.master.model.persistence.hibernate;

import com.zutubi.pulse.core.model.PersistentChangelist;
import com.zutubi.pulse.core.scm.api.Changelist;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.model.persistence.ChangelistDao;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.SessionFactoryUtils;

import java.util.*;

/**
 * Hibernate implementation of ChangelistDao.
 */
@SuppressWarnings({"unchecked"})
public class HibernateChangelistDao extends HibernateEntityDao<PersistentChangelist> implements ChangelistDao
{
    public Class persistentClass()
    {
        return PersistentChangelist.class;
    }

    public Set<Long> getAllAffectedProjectIds(PersistentChangelist changelist)
    {
        List<PersistentChangelist> all = findAllEquivalent(changelist);
        Set<Long> ids = new HashSet<Long>();
        for(PersistentChangelist cl: all)
        {
            ids.add(cl.getProjectId());
        }

        return ids;
    }

    public Set<Long> getAllAffectedResultIds(PersistentChangelist changelist)
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
        List<PersistentChangelist> result = (List<PersistentChangelist>) getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException
            {
                Query queryObject = session.createQuery("from PersistentChangelist model where model.hash = :hash");
                queryObject.setParameter("hash", changelist.getHash());

                SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());

                return queryObject.list();
            }
        });

        // Now eliminate false-positives from hash collisions.
        Changelist rawChangelist = changelist.asChangelist();
        for (Iterator<PersistentChangelist> it = result.iterator(); it.hasNext(); )
        {
            PersistentChangelist current = it.next();
            if (!current.asChangelist().equals(rawChangelist))
            {
                it.remove();
            }
        }

        return result;
    }

    public List<PersistentChangelist> findByResult(final long id)
    {
        return  (List<PersistentChangelist>) getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException
            {
                Query queryObject = session.createQuery("from PersistentChangelist model where model.resultId = :resultId order by model.time desc, model.id desc");
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

        while(results.size() < max)
        {
            List<PersistentChangelist> changelists = (List<PersistentChangelist>) getHibernateTemplate().execute(new HibernateCallback()
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

    private void addUnique(Collection<PersistentChangelist> lists, Collection<PersistentChangelist> toAdd)
    {
        for(PersistentChangelist candidate: toAdd)
        {
            boolean found = false;
            for(PersistentChangelist existing: lists)
            {
                if(existing.asChangelist().equals(candidate.asChangelist()))
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
