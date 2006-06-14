package com.zutubi.pulse.model.persistence.hibernate;

import com.zutubi.pulse.core.model.Resource;
import com.zutubi.pulse.model.persistence.ResourceDao;
import com.zutubi.pulse.model.Slave;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.PersistentResource;

import java.util.List;

import org.springframework.orm.hibernate3.HibernateCallback;
import org.hibernate.Session;
import org.hibernate.HibernateException;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Expression;

/**
 */
public class HibernateResourceDao extends HibernateEntityDao<PersistentResource> implements ResourceDao
{
    public Class persistentClass()
    {
        return PersistentResource.class;
    }


    public List<PersistentResource> findAllBySlave(final Slave slave)
    {
        return (List<PersistentResource>) getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException
            {
                Criteria criteria = createSlaveCriteria(session, slave);
                return criteria.list();
            }
        });
    }

    public PersistentResource findBySlaveAndName(final Slave slave, final String name)
    {
        return (PersistentResource) getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException
            {
                Criteria criteria = createSlaveCriteria(session, slave);
                criteria.add(Expression.eq("name", name));
                return criteria.uniqueResult();
            }
        });
    }

    private Criteria createSlaveCriteria(Session session, Slave slave)
    {
        Criteria criteria = session.createCriteria(PersistentResource.class);
        if(slave == null)
        {
            criteria.add(Expression.isNull("slave"));
        }
        else
        {
            criteria.add(Expression.eq("slave", slave));
        }
        
        return criteria;
    }

}
