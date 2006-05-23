/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.search;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projection;
import org.hibernate.criterion.Projections;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.SessionFactoryUtils;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

/**
 * <class-comment/>
 */
public class SearchQuery<T> implements Serializable
{
    private List<Criterion> criterion = new LinkedList<Criterion>();

    private List<Order> ordering = new LinkedList<Order>();

    private Projection projection;

    private Class target;

    private HibernateTemplate hibernateTemplate;

    private SessionFactory sessionFactory;

    private static final int NOT_SPECIFIED = -1;

    private int firstResult = NOT_SPECIFIED;

    private int maxResults = NOT_SPECIFIED;

    public SearchQuery(Class target)
    {
        this.target = target;
    }

    public void setFirstResult(int firstResult)
    {
        this.firstResult = firstResult;
    }

    public void setMaxResults(int maxResults)
    {
        this.maxResults = maxResults;
    }

    public SearchQuery add(Criterion expression)
    {
        criterion.add(expression);
        return this;
    }

    public SearchQuery add(Order order)
    {
        ordering.add(order);
        return this;
    }

    public void setProjection(Projection projection)
    {
        this.projection = projection;
    }

    public List<T> list()
    {
        return (List<T>)getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException, SQLException
            {
                Criteria criteria = session.createCriteria(target);
                configureQuery(criteria);
                return criteria.list();
            }
        });
    }

    public T uniqueResult()
    {
        return (T)getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException, SQLException
            {
                Criteria criteria = session.createCriteria(target);
                configureQuery(criteria);
                return criteria.uniqueResult();
            }
        });
    }

    private void configureQuery(Criteria criteria)
    {
        if (firstResult != NOT_SPECIFIED)
        {
            criteria.setFirstResult(firstResult);
        }
        if (maxResults != NOT_SPECIFIED)
        {
            criteria.setMaxResults(maxResults);
        }
        for (Criterion c : criterion)
        {
            criteria.add(c);
        }
        for (Order o : ordering)
        {
            criteria.addOrder(o);
        }

        if (projection != null)
        {
            criteria.setProjection(projection);
        }

        SessionFactoryUtils.applyTransactionTimeout(criteria, sessionFactory);
    }

    protected HibernateTemplate getHibernateTemplate()
    {
        return hibernateTemplate;
    }

    protected SessionFactory getSessionFactory()
    {
        return sessionFactory;
    }

    public void setSessionFactory(SessionFactory sessionFactory)
    {
        this.sessionFactory = sessionFactory;
        hibernateTemplate = new HibernateTemplate(sessionFactory);
    }

    public int count()
    {
        return (Integer)getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException
            {
                Criteria criteria = session.createCriteria(target);
                for (Criterion c : criterion)
                {
                    criteria.add(c);
                }
                criteria.setProjection(Projections.rowCount());
                return criteria.uniqueResult();
            }
        });
    }
}
