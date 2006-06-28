package com.zutubi.pulse.model.persistence.hibernate;

import com.zutubi.pulse.model.CommitMessageTransformer;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.persistence.CommitMessageTransformerDao;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.SessionFactoryUtils;

import java.util.List;

/**
 */
public class HibernateCommitMessageTransformerDao extends HibernateEntityDao<CommitMessageTransformer> implements CommitMessageTransformerDao
{
    public Class persistentClass()
    {
        return CommitMessageTransformer.class;
    }

    public List<CommitMessageTransformer> findByProject(final Project project)
    {
        return (List<CommitMessageTransformer>) getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException
            {
                Query queryObject = session.createQuery("from CommitMessageTransformer model where :projectId in elements(model.projects)");
                queryObject.setParameter("projectId", project.getId());
                SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());
                return queryObject.list();
            }
        });
    }

    public CommitMessageTransformer findByName(final String name)
    {
        return (CommitMessageTransformer) getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException
            {
                Query queryObject = session.createQuery("from CommitMessageTransformer model where model.name = :name");
                queryObject.setParameter("name", name);
                SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());
                return queryObject.uniqueResult();
            }
        });
    }
}
