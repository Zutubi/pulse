package com.cinnamonbob.model.persistence.hibernate;

import com.cinnamonbob.model.Entity;
import com.cinnamonbob.model.persistence.EntityDao;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

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

    public abstract Class persistentClass();
}
