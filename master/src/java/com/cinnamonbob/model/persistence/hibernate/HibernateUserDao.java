package com.cinnamonbob.model.persistence.hibernate;

import com.cinnamonbob.model.User;
import com.cinnamonbob.model.persistence.UserDao;
import org.hibernate.*;
import org.hibernate.criterion.Projections;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.SessionFactoryUtils;

import java.util.List;
import com.cinnamonbob.util.logging.Logger;

/**
 * 
 *
 */
public class HibernateUserDao extends HibernateEntityDao<User> implements UserDao
{
    private static final Logger LOG = Logger.getLogger(HibernateEntityDao.class);

    public Class persistentClass()
    {
        return User.class;
    }

    public User findByLogin(final String login)
    {
        List users = (List) getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
                Query queryObject = session.createQuery("from User user where user.login = :login");
                queryObject.setParameter("login", login, Hibernate.STRING);

                SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());

                return queryObject.list();
            }
        });

        if (users.size() > 1)
        {
            LOG.warning("findByLogin has returned " + users.size() +
                    " results when expecting at most one.");
        }
        if (users.size() > 0)
        {
            return (User) users.get(0);
        }
        return null;
    }

    public List<User> findByLikeLogin(final String login)
    {
        return (List<User>) getHibernateTemplate().execute(new HibernateCallback(){
            public Object doInHibernate(Session session) throws HibernateException 
            {
                Query queryObject = session.createQuery("from User user where user.login like :login");
                queryObject.setParameter("login", login, Hibernate.STRING);
                
                SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());
                
                return queryObject.list();
            }
        });
    }

    public int getUserCount()
    {
        return (Integer) getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException
            {
                Criteria criteria = session.createCriteria(User.class);
                criteria.setProjection(Projections.rowCount());
                return criteria.uniqueResult();
            }
        });
    }
}
