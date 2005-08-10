package com.cinnamonbob.model.persistence.hibernate;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.SessionFactoryUtils;

import com.cinnamonbob.core.BuildResult;
import com.cinnamonbob.model.User;
import com.cinnamonbob.model.persistence.BuildResultDao;

public class HibernateBuildResultDao extends HibernateEntityDao<BuildResult> implements BuildResultDao
{
    private static final Logger LOG = Logger.getLogger(HibernateEntityDao.class.getName());

    @Override
    public Class persistentClass()
    {
        return BuildResult.class;
    }
    
    public List findLatestByProjectName(final String project, final int max)
    {
        return (List)getHibernateTemplate().execute(new HibernateCallback(){
            public Object doInHibernate(Session session) throws HibernateException, SQLException
            {
                Query queryObject = session.createQuery("from BuildResult result where result.projectName = :project and result.stateName = 'COMPLETED' order by id desc");
                queryObject.setParameter("project", project, Hibernate.STRING);
                queryObject.setMaxResults(max);
                
                SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());
                
                return queryObject.list();
            }
        });
    }

    public BuildResult findByProjectNameAndNumber(final String project, final long number)
    {
        List results = (List)getHibernateTemplate().execute(new HibernateCallback(){
            public Object doInHibernate(Session session) throws HibernateException, SQLException
            {
                Query queryObject = session.createQuery("from BuildResult result where result.projectName = :project and result.number = :number");
                queryObject.setParameter("project", project, Hibernate.STRING);
                queryObject.setParameter("number", number, Hibernate.LONG);
                
                SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());
                
                return queryObject.list();
            }
        });
        
        if(results.size() > 1)
        {
            LOG.warning("findByProjectNameAndNumber has returned " + results.size() +
                    " results when expecting at most one.");
        }
        if(results.size() > 0)
        {
            return (BuildResult)results.get(0);
        }
        return null;

    }

}
