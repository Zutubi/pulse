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

    public List<Changelist> findLatestByUser(final User user, final int max)
    {
        return (List<Changelist>) getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException
            {
                Query queryObject = session.createQuery("from Changelist model where model.revision.author in (:logins) order by model.revision.time desc");
                List<String> allLogins = new LinkedList<String>();
                allLogins.add(user.getLogin());
                allLogins.addAll(user.getAliases());
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
}
