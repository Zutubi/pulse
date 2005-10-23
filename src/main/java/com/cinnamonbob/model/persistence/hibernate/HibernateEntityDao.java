package com.cinnamonbob.model.persistence.hibernate;

import com.cinnamonbob.model.Entity;
import com.cinnamonbob.model.persistence.EntityDao;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.hibernate.Query;
import org.hibernate.HibernateException;
import org.hibernate.Session;

import java.util.List;

/**
 * 
 *
 */
public abstract class HibernateEntityDao<T extends Entity> extends HibernateDaoSupport implements EntityDao<T>
{
    public T findById(long id)
    {
        return (T) getHibernateTemplate().load(persistentClass(), Long.valueOf(id));
    }

    public List<T> findAll()
    {
        return (List<T>)getHibernateTemplate().find("from " + persistentClass().getName());
    }
    
    public void save(T entity)
    {
        getHibernateTemplate().saveOrUpdate(entity);
    }

    public void delete(T entity)
    {
        getHibernateTemplate().delete(entity);
    }

    public void refresh(T entity)
    {
        getHibernateTemplate().refresh(entity);
    }

    public <U> List<U> findByNamedQuery(final String queryName)
    {
        return findByNamedQuery(queryName, 0);
    }

    public <U> List<U> findByNamedQuery(final String queryName, final int maxResults)
    {
        return findByNamedQuery(queryName, maxResults, false);
    }

    public <U> List<U> findByNamedQuery(final String queryName, final int maxResults, final boolean cachable)
    {
        return findByNamedQuery(queryName, null, null, maxResults, cachable);
    }

    public <U> List<U> findByNamedQuery(final String queryName, final String propertyName, final Object propertyValue)
    {
        return findByNamedQuery(queryName, propertyName, propertyValue, 0);
    }

    public <U> List<U> findByNamedQuery(final String queryName, final String propertyName, final Object propertyValue, final int maxResults)
    {
        return findByNamedQuery(queryName, propertyName, propertyValue, maxResults, false);
    }

    public <U> List<U> findByNamedQuery(final String queryName, final String propertyName, final Object propertyValue, final int maxResults, final boolean cachable)
    {
        return (List<U>)getHibernateTemplate().execute(new HibernateCallback()
         {
             public Object doInHibernate(Session session) throws HibernateException
             {
                 Query queryObject = session.getNamedQuery(queryName);
                 if (propertyName != null)
                 {
                     queryObject.setParameter(propertyName, propertyValue);
                 }
                 if (maxResults > 0)
                 {
                     queryObject.setMaxResults(maxResults);
                 }
                 queryObject.setCacheable(cachable);

                 SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());
                 return queryObject.list();
             }
         });
    }

    public final Object findUniqueByNamedQuery(final String queryName)
    {
        return findUniqueByNamedQuery(queryName, false);
    }

    public final Object findUniqueByNamedQuery(final String queryName, final boolean cachable)
    {
        return findUniqueByNamedQuery(queryName, null, null, cachable);
    }

    public final Object findUniqueByNamedQuery(final String queryName, final String propertyName, final Object propertyValue)
    {
        return findUniqueByNamedQuery(queryName, propertyName, propertyValue, false);
    }

    public final Object findUniqueByNamedQuery(final String queryName, final String propertyName, final Object propertyValue, final boolean cachable)
    {
        return getHibernateTemplate().execute(new HibernateCallback()
         {
             public Object doInHibernate(Session session) throws HibernateException
             {
                 Query queryObject = session.getNamedQuery(queryName);
                 if (propertyName != null)
                 {
                     queryObject.setParameter(propertyName, propertyValue);
                 }
                 queryObject.setCacheable(cachable);
                 SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());
                 return queryObject.uniqueResult();
             }
         });
    }

    public abstract Class persistentClass();
}
