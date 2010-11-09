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
 * 
 *
 */
public abstract class HibernateEntityDao<T extends Entity> extends HibernateDaoSupport implements EntityDao<T>
{
    private static final Logger LOG = Logger.getLogger(HibernateEntityDao.class);

    @SuppressWarnings({"unchecked"})
    public T findById(long id)
    {
        return (T) findByIdAndType(id, persistentClass());
    }

    @SuppressWarnings({"unchecked"})
    protected <U extends T> U findByIdAndType(long id, Class<U> type)
    {
        return (U)findAnyType(id, type);
    }

    @SuppressWarnings({"unchecked"})
    protected <T> T findAnyType(long id, Class type)
    {
        try
        {
            return (T)getHibernateTemplate().load(type, Long.valueOf(id));
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

    @SuppressWarnings({"unchecked"})
    public List<T> findAll()
    {
        return (List<T>) getHibernateTemplate().loadAll(persistentClass());
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
        return (Integer) getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException
            {
                Criteria criteria = session.createCriteria(persistentClass());
                criteria.setProjection(Projections.rowCount());
                SessionFactoryUtils.applyTransactionTimeout(criteria, getSessionFactory());
                return criteria.uniqueResult();
            }
        });
    }

    public Object findFirstByNamedQuery(final String queryName)
    {
        return findFirstByNamedQuery(queryName, false);
    }

    public Object findFirstByNamedQuery(final String queryName, final boolean cachable)
    {
        return findFirstByNamedQuery(queryName, null, null, cachable);
    }

    public Object findFirstByNamedQuery(final String queryName, final String propertyName, final Object propertyValue)
    {
        return findFirstByNamedQuery(queryName, propertyName, propertyValue, false);
    }

    @SuppressWarnings({"unchecked"})
    public Object findFirstByNamedQuery(final String queryName, final String propertyName, final Object propertyValue, final boolean cacheable)
    {
        List<Object> results = (List<Object>) getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException
            {
                Query queryObject = session.getNamedQuery(queryName);
                if (propertyName != null)
                {
                    queryObject.setParameter(propertyName, propertyValue);
                }
                queryObject.setMaxResults(1);
                queryObject.setCacheable(cacheable);

                SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());
                return queryObject.list();
            }
        });
        if (results != null && results.size() > 0)
        {
            return results.get(0);
        }
        return null;
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

    @SuppressWarnings({"unchecked"})
    public <U> List<U> findByNamedQuery(final String queryName, final String propertyName, final Object propertyValue, final int maxResults, final boolean cachable)
    {
        return (List<U>) getHibernateTemplate().execute(new HibernateCallback()
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
        return findUniqueByNamedQuery(queryName, propertyName, propertyValue, null, null, cachable);
    }

    public final Object findUniqueByNamedQuery(final String queryName, final String propertyName, final Object propertyValue, final String secondPropertyName, final Object secondPropertyValue)
    {
        return findUniqueByNamedQuery(queryName, propertyName, propertyValue, secondPropertyName, secondPropertyValue, false);
    }

    @SuppressWarnings({"unchecked"})
    public final <U> U findUniqueByNamedQuery(final String queryName, final String propertyName, final Object propertyValue, final String secondPropertyName, final Object secondPropertyValue, final boolean cachable)
    {
        return (U) getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException
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
                return queryObject.uniqueResult();
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

    public abstract Class persistentClass();
}
