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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * Hibernate implementation of ChangelistDao.
 */
public class HibernateChangelistDao extends HibernateEntityDao<Changelist> implements ChangelistDao
{
    public Class persistentClass()
    {
        return Changelist.class;
    }

    @SuppressWarnings({ "unchecked" })
    public List<Changelist> findLatestByUser(final User user, final int max)
    {
        return (List<Changelist>) getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException
            {
                Query queryObject = session.createQuery("from Changelist model where model.revision.author in (:logins) order by model.revision.time desc");
                List<String> allLogins = new LinkedList<String>();
                allLogins.add(user.getConfig().getLogin());
                allLogins.addAll(user.getConfig().getPreferences().getSettings().getAliases());
                queryObject.setParameterList("logins", allLogins);
                queryObject.setMaxResults(max);

                SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());

                return queryObject.list();
            }
        });
    }

    public List<Changelist> findLatestByProject(final Project project, final int max)
    {
        return (List<Changelist>) getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException
            {
                Query queryObject = session.createQuery("from Changelist model where :projectId in elements(model.projectIds) order by model.revision.time desc");
                queryObject.setParameter("projectId", project.getId());
                queryObject.setMaxResults(max);

                SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());

                return queryObject.list();
            }
        });
    }

    public List<Changelist> findLatestByProjects(Project[] projects, final int max)
    {
        final Long[] projectIds = new Long[projects.length];
        for(int i = 0; i < projects.length; i++)
        {
            projectIds[i] = projects[i].getId();
        }

        LinkedHashSet<Changelist> results = new LinkedHashSet<Changelist>();
        final int[] offset = { 0 };

        while(results.size() < max)
        {
            List<Changelist> changelists = (List<Changelist>) getHibernateTemplate().execute(new HibernateCallback()
            {
                public Object doInHibernate(Session session) throws HibernateException
                {
                    Query queryObject = session.createQuery("from Changelist model join model.projectIds project where project in (:projectIds) order by model.revision.time desc");
                    queryObject.setParameterList("projectIds", projectIds);
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

            results.addAll(changelists);
            offset[0] = offset[0] + max;
        }

        // We can actually get up to max - 1 extra entries if multiple queries
        // were run, so discard any after max (better than shrinking query max
        // as we go as then we may execute many queries).
        ArrayList<Changelist> changes = new ArrayList<Changelist>(results);
        while(changes.size() > max)
        {
            changes.remove(changes.size() - 1);
        }
        return changes;
    }

    public Changelist findByRevision(final String serverUid, final Revision revision)
    {
        return (Changelist) getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException
            {
                Query queryObject = session.createQuery("from Changelist model where model.serverUid = :serverUid and model.revision.revisionString = :revisionString");
                queryObject.setParameter("serverUid", serverUid);
                queryObject.setParameter("revisionString", revision.getRevisionString());

                SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());

                return queryObject.uniqueResult();
            }
        });
    }

    public List<Changelist> findByResult(final long id)
    {
        List<Changelist> all = (List<Changelist>) getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException
            {
                Query queryObject = session.createQuery("from Changelist model where :resultId in elements(model.resultIds) order by model.revision.time desc");
                queryObject.setParameter("resultId", id);

                SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());

                return queryObject.list();
            }
        });

        // Using the fetch join means we no longer get distinct results!
        LinkedHashSet<Changelist> set = new LinkedHashSet<Changelist>(all);
        for (Changelist changelist : all)
        {
            changelist.getChanges().size();
            set.add(changelist);
        }

        return new LinkedList<Changelist>(set);
    }
}
