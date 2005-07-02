package com.cinnamonbob.model.persistence.hibernate;

import com.cinnamonbob.model.Entity;
import com.cinnamonbob.model.persistence.EntityDao;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import java.util.List;

/**
 * 
 *
 */
public abstract class HibernateEntityDao extends HibernateDaoSupport implements EntityDao
{
    public Entity findById(long id)
    {
        return (Entity) getHibernateTemplate().load(persistentClass(), Long.valueOf(id));
    }

    public List findAll()
    {
        return getHibernateTemplate().find("from " + persistentClass().getName());
    }
    
    public void save(Entity entity)
    {
        getHibernateTemplate().save(entity);
    }
    
    public abstract Class persistentClass();
}
