package com.zutubi.pulse.model.persistence.hibernate;

import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.User;
import com.zutubi.pulse.model.Group;
import com.zutubi.pulse.model.persistence.UserDao;
import com.zutubi.pulse.util.logging.Logger;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.SessionFactoryUtils;

import java.util.List;
import java.util.Set;

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

    public Set<Project> getHiddenProjects(final User user)
    {
        User u = (User) getHibernateTemplate().execute(new HibernateCallback(){
            public Object doInHibernate(Session session) throws HibernateException
            {
                Query queryObject = session.createQuery("from User user left join fetch user.hiddenProjects where user.id = :id");
                queryObject.setParameter("id", user.getId());

                SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());

                return queryObject.uniqueResult();
            }
        });

        return u.getHiddenProjects();
    }

    public List<User> findByNotInGroup(final Group group)
    {
        return (List<User>) getHibernateTemplate().execute(new HibernateCallback(){
            public Object doInHibernate(Session session) throws HibernateException
            {
                Query queryObject = session.createQuery("from User user where :group not in elements(user.groups)");
                queryObject.setEntity("group", group);
                SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());
                return queryObject.list();
            }
        });
    }

    public List<User> findByHiddenProject(final Project project)
    {
        return (List<User>) getHibernateTemplate().execute(new HibernateCallback(){
            public Object doInHibernate(Session session) throws HibernateException
            {
                Query queryObject = session.createQuery("from User user where :project in elements(user.hiddenProjects)");
                queryObject.setEntity("project", project);
                SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());
                return queryObject.list();
            }
        });
    }
}
