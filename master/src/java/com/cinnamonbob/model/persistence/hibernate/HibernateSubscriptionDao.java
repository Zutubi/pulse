package com.cinnamonbob.model.persistence.hibernate;

import com.cinnamonbob.model.Project;
import com.cinnamonbob.model.Subscription;
import com.cinnamonbob.model.persistence.SubscriptionDao;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.SessionFactoryUtils;

import java.util.List;

/**
 *
 *
 */
public class HibernateSubscriptionDao extends HibernateEntityDao<Subscription> implements SubscriptionDao
{
    public Class persistentClass()
    {
        return Subscription.class;
    }

    public List<Subscription> findByProject(final Project project)
    {
        return (List<Subscription>) getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException
            {
                Query queryObject = session.getNamedQuery("subscription.findByProject");
                queryObject.setParameter("project", project);
                queryObject.setCacheable(true);

                SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());
                return queryObject.list();
            }
        });
    }

    public int deleteByProject(final Project project)
    {
        return (Integer) getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException
            {
                Query queryObject = session.getNamedQuery("subscription.deleteByProject");
                queryObject.setParameter("project", project);

                SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());
                return queryObject.executeUpdate();
            }
        });
    }
}
