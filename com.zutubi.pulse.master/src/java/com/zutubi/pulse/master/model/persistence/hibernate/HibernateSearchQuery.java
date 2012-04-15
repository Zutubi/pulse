package com.zutubi.pulse.master.model.persistence.hibernate;

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
import java.util.LinkedList;
import java.util.List;

/**
 * The SearchQuery provides access to the hibernate Criteria API to allow for arbitrary search queries to be
 * executed.
 *
 * In implementation, it is a wrapper around the Criteria API that handles the hibernate session management.
 */
public class HibernateSearchQuery<T> implements Serializable
{
    /**
     * The list of Criterion that make up this query.
     */
    private List<Criterion> criterion = new LinkedList<Criterion>();

    /**
     * The order clauses associated with this query.
     */
    private List<Order> ordering = new LinkedList<Order>();

    /**
     * The projection to be applied to the result of this query.
     */
    private Projection projection;

    /**
     * The type of class that will be retrieved by this query.
     */
    private Class target;

    /**
     * The hibernate template is used to help manage the hibernate resources.
     */
    private HibernateTemplate hibernateTemplate;

    /**
     * The session factory provides access to the required hibernate resources.
     */
    private SessionFactory sessionFactory;

    /**
     * NOT_SPECIFIED is used to indicate that an integer value has not been specified.
     */
    private static final int NOT_SPECIFIED = -1;

    /**
     * The index of the first result to be returned by this query.
     */
    private int firstResult = NOT_SPECIFIED;

    /**
     * The maximum number of results to be returned by this query.
     */
    private int maxResults = NOT_SPECIFIED;

    /**
     * Create a new query instance that can be used to lookup data of the
     * target type.
     *
     * @param target    the type of data to be queried.
     */
    public HibernateSearchQuery(Class target)
    {
        this.target = target;
    }

    /**
     * Specify the index of the first result within the results to be returned when this query is executed.
     *
     * @param firstResult   the index of the first result
     *
     * @see Criteria#setFirstResult(int)
     */
    public void setFirstResult(int firstResult)
    {
        this.firstResult = firstResult;
    }

    /**
     * Specify the maximum number of results to be returned by this search query.
     *
     * @param maxResults    the max number of results
     *
     * @see Criteria#setMaxResults(int)
     */
    public void setMaxResults(int maxResults)
    {
        this.maxResults = maxResults;
    }

    /**
     * Add a criterion to this search query.
     *
     * @param expression    the criterion expression to be added to this query.
     *
     * @return the modified search query instance.
     *
     * @see Criteria#add(org.hibernate.criterion.Criterion)
     */
    public HibernateSearchQuery add(Criterion expression)
    {
        criterion.add(expression);
        return this;
    }

    /**
     * Add an Order clause to this search query.
     *
     * @param order the order clause to add to this query
     *
     * @return the modified search query instance.
     *
     * @see Criteria#addOrder(org.hibernate.criterion.Order)
     */
    public HibernateSearchQuery add(Order order)
    {
        ordering.add(order);
        return this;
    }

    /**
     * Add a projection to this search query.
     *
     * @param projection    the projection to add to this query
     *
     * @return the modified search query instance.
     *
     * @see Criteria#setProjection(org.hibernate.criterion.Projection)
     */
    public HibernateSearchQuery setProjection(Projection projection)
    {
        this.projection = projection;
        return this;
    }

    /**
     * Execute this search query and return the list of matching elements.
     *
     * @return the elements matching the search criterion.
     */
    public List<T> list()
    {
        return (List<T>)getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException
            {
                Criteria criteria = session.createCriteria(target);
                configureQuery(criteria);
                return criteria.list();
            }
        });
    }

    /**
     * Execute this search query and return the unique match.
     *
     * @return the unique result matched by this search query.
     */
    public T uniqueResult()
    {
        return (T)getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException
            {
                Criteria criteria = session.createCriteria(target);
                configureQuery(criteria);
                return criteria.uniqueResult();
            }
        });
    }

    /**
     * Returns a count of the number of elements that match the criterion of this query.
     *
     * @return the number of elements matched by this query.
     */
    public long count()
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

    protected void configureQuery(Criteria criteria)
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

}
