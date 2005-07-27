package com.cinnamonbob.model.persistence.hibernate;

import com.cinnamonbob.model.Project;
import com.cinnamonbob.model.persistence.ProjectDao;
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
public class HibernateProjectDao extends HibernateEntityDao implements ProjectDao
{
    private static final Logger LOG = Logger.getLogger(HibernateEntityDao.class.getName());

    public Class persistentClass()
    {
        return Project.class;
    }

    public Project findByName(final String name)
    {
        List projects = (List) getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
                Query queryObject = session.createQuery("from Project project where project.name = :name");
                queryObject.setParameter("name", name, Hibernate.STRING);

                SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());

                return queryObject.list();
            }
        });

        if (projects.size() > 1)
        {
            LOG.warning("findByName has returned " + projects.size() +
                    " results when expecting at most one.");
        }
        if (projects.size() > 0)
        {
            return (Project) projects.get(0);
        }
        return null;
    }

    public List findByLikeName(final String name)
    {
        return (List)getHibernateTemplate().execute(new HibernateCallback(){
            public Object doInHibernate(Session session) throws HibernateException, SQLException
            {
                Query queryObject = session.createQuery("from Project project where project.name like :name");
                queryObject.setParameter("name", name, Hibernate.STRING);

                SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());

                return queryObject.list();
            }
        });
    }}
