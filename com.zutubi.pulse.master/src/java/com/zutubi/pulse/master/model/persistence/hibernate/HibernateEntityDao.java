/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.model.persistence.hibernate;

import com.zutubi.pulse.core.model.Entity;
import com.zutubi.pulse.master.model.persistence.EntityDao;
import com.zutubi.util.logging.Logger;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.springframework.orm.hibernate4.HibernateObjectRetrievalFailureException;
import org.springframework.orm.hibernate4.support.HibernateDaoSupport;

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

    protected <U> U findAnyType(long id, Class<U> type)
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

    public long count()
    {
        return (Long) getSessionFactory().getCurrentSession()
                .createCriteria(persistentClass())
                .setProjection(Projections.rowCount())
                .uniqueResult();
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
        Session session = getSessionFactory().getCurrentSession();
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
        return queryObject.list();
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
        Session session = getSessionFactory().getCurrentSession();
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
        return (U) queryObject.uniqueResult();
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
