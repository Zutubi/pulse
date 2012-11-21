package com.zutubi.pulse.master.model.persistence.hibernate;

import com.zutubi.pulse.core.model.Entity;
import com.zutubi.pulse.master.model.persistence.EntityDao;
import com.zutubi.util.logging.Logger;
import org.hibernate.*;
import org.hibernate.criterion.Projections;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateObjectRetrievalFailureException;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import java.util.List;

/**
 * Helper base class for implementing DAOs with Hibernate.  Provides basic CRUD support along with a
 * collection of utility/convenience methods.
 */
public abstract class HibernateEntityDao<T extends Entity> extends HibernateDaoSupport implements EntityDao<T>
{
    private static final Logger LOG = Logger.getLogger(HibernateEntityDao.class);

    public T findById(long id)
    {
        return findByIdAndType(id, persistentClass());
    }

    protected <U extends T> U findByIdAndType(long id, Class<U> type)
    {
        return findAnyType(id, type);
    }

    protected <T> T findAnyType(long id, Class<T> type)
    {
        try
        {
            return getHibernateTemplate().load(type, Long.valueOf(id));
        }
        catch (ObjectNotFoundException e)
        {
            return null;
        }
        catch (HibernateObjectRetrievalFailureException e)
        {
            return null;
        }
    }

    public List<T> findAll()
    {
        return getHibernateTemplate().loadAll(persistentClass());
    }

    public void save(T entity)
    {
        getHibernateTemplate().saveOrUpdate(entity);
    }

    public void flush()
    {
        getHibernateTemplate().flush();
    }

    public void delete(T entity)
    {
        getHibernateTemplate().delete(entity);
    }

    public void refresh(T entity)
    {
        getHibernateTemplate().refresh(entity);
    }

    public int count()
    {
        return getHibernateTemplate().execute(new HibernateCallback<Integer>()
        {
            public Integer doInHibernate(Session session) throws HibernateException
            {
                Criteria criteria = session.createCriteria(persistentClass());
                criteria.setProjection(Projections.rowCount());
                SessionFactoryUtils.applyTransactionTimeout(criteria, getSessionFactory());
                return (Integer) criteria.uniqueResult();
            }
        });
    }

    public <U> List<U> findByNamedQuery(final String queryName, final String propertyName, final Object propertyValue)
    {
        return findByNamedQuery(queryName, propertyName, propertyValue, 0);
    }

    public <U> List<U> findByNamedQuery(final String queryName, final String propertyName, final Object propertyValue, final int maxResults)
    {
        return findByNamedQuery(queryName, propertyName, propertyValue, maxResults, false);
    }

    @SuppressWarnings({"unchecked"})
    public <U> List<U> findByNamedQuery(final String queryName, final String propertyName, final Object propertyValue, final int maxResults, final boolean cachable)
    {
        return getHibernateTemplate().execute(new HibernateCallback<List<U>>()
        {
            public List<U> doInHibernate(Session session) throws HibernateException
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

    public final Object findUniqueByNamedQuery(final String queryName, final String propertyName, final Object propertyValue)
    {
        return findUniqueByNamedQuery(queryName, propertyName, propertyValue, false);
    }

    public final Object findUniqueByNamedQuery(final String queryName, final String propertyName, final Object propertyValue, final boolean cachable)
    {
        return findUniqueByNamedQuery(queryName, propertyName, propertyValue, null, null, cachable);
    }

    public final Object findUniqueByNamedQuery(final String queryName, final String propertyName, final Object propertyValue, final String secondPropertyName, final Object secondPropertyValue)
    {
        return findUniqueByNamedQuery(queryName, propertyName, propertyValue, secondPropertyName, secondPropertyValue, false);
    }

    @SuppressWarnings({"unchecked"})
    public final <U> U findUniqueByNamedQuery(final String queryName, final String propertyName, final Object propertyValue, final String secondPropertyName, final Object secondPropertyValue, final boolean cachable)
    {
        return getHibernateTemplate().execute(new HibernateCallback<U>()
        {
            public U doInHibernate(Session session) throws HibernateException
            {
                Query queryObject = session.getNamedQuery(queryName);
                if (propertyName != null)
                {
                    queryObject.setParameter(propertyName, propertyValue);
                }
                if (secondPropertyName != null)
                {
                    queryObject.setParameter(secondPropertyName, secondPropertyValue);
                }
                queryObject.setCacheable(cachable);
                SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());
                return (U) queryObject.uniqueResult();
            }
        });
    }

    /**
     * Temporary method used to shrink long values to integers.  If the long value is
     * greater than the maximum integer value, Integer.MAX_VALUE is returned.
     *
     * @param l     the long value being 'squeezed' into an int field.
     * @return the integer equivalent of the long value, or Integer.MAX_VALUE
     */
    protected int toInt(long l)
    {
        if (l > Integer.MAX_VALUE)
        {
            LOG.warning("toInt limit reached.  Truncating value (" + l + ") to Integer.MAX_VALUE");
            return Integer.MAX_VALUE;
        }
        return (int)l;
    }

    public abstract Class<T> persistentClass();
}
