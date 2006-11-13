package com.zutubi.pulse.model.persistence.hibernate;

import com.zutubi.pulse.model.BuildSpecificationNode;
import com.zutubi.pulse.model.persistence.BuildSpecificationNodeDao;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.SessionFactoryUtils;

import java.util.List;

/**
 */
public class HibernateBuildSpecificationNodeDao extends HibernateEntityDao<BuildSpecificationNode> implements BuildSpecificationNodeDao
{
    public Class persistentClass()
    {
        return BuildSpecificationNode.class;
    }

    public List<BuildSpecificationNode> findByResourceRequirement(final String resource)
    {
        return (List<BuildSpecificationNode>) getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException
            {
                Query queryObject = session.createQuery("from BuildSpecificationNode model join model.resourceRequirements requirement where requirement.resource = :resource");
                queryObject.setParameter("resource", resource);
                SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());
                return queryObject.list();
            }
        });
    }

}
