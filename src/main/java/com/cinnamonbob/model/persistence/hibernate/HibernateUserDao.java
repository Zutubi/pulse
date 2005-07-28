package com.cinnamonbob.model.persistence.hibernate;

import com.cinnamonbob.model.User;
import com.cinnamonbob.model.persistence.UserDao;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.SessionFactoryUtils;

import java.util.List;
import java.util.logging.Logger;
import java.sql.SQLException;

/**
 * 
 *
 */
public class HibernateUserDao extends HibernateEntityDao<User> implements UserDao
{
    private static final Logger LOG = Logger.getLogger(HibernateEntityDao.class.getName());

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

    public List findByLikeLogin(final String login)
    {
        return (List)getHibernateTemplate().execute(new HibernateCallback(){
            public Object doInHibernate(Session session) throws HibernateException, SQLException
            {
                Query queryObject = session.createQuery("from User user where user.login like :login");
                queryObject.setParameter("login", login, Hibernate.STRING);
                
                SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());
                
                return queryObject.list();
            }
        });
    }
}
