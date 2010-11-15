package com.zutubi.pulse.master.model.persistence.hibernate;

import com.zutubi.pulse.core.model.TestCaseIndex;
import com.zutubi.pulse.master.model.persistence.TestCaseIndexDao;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.SessionFactoryUtils;

import java.util.List;

/**
 */
public class HibernateTestCaseIndexDao extends HibernateEntityDao<TestCaseIndex> implements TestCaseIndexDao
{
    public Class persistentClass()
    {
        return TestCaseIndex.class;
    }

    public TestCaseIndex findByCase(final long stageNameId, final String name)
    {
        return (TestCaseIndex) getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException
            {
                Query queryObject = session.createQuery("from TestCaseIndex model where model.stageNameId = :stageNameId and model.name = :name");
                queryObject.setParameter("stageNameId", stageNameId);
                queryObject.setParameter("name", name);

                SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());

                return queryObject.uniqueResult();
            }
        });
    }

    public List<TestCaseIndex> findBySuite(final long stageNameId, final String suite)
    {
        return (List<TestCaseIndex>) getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException
            {
                Query queryObject = session.createQuery("from TestCaseIndex model where model.stageNameId = :stageNameId and model.name like :suite");
                queryObject.setParameter("stageNameId", stageNameId);
                queryObject.setParameter("suite", suite + "%");

                SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());

                return queryObject.list();
            }
        });
    }

    public List<TestCaseIndex> findByStage(final long stageNameId)
    {
        return (List<TestCaseIndex>) getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException
            {
                Query queryObject = session.createQuery("from TestCaseIndex model where model.stageNameId = :stageNameId");
                queryObject.setParameter("stageNameId", stageNameId);
                SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());

                return queryObject.list();
            }
        });
    }

    public int deleteByProject(final long projectId)
    {
        return (Integer) getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException
            {
                Query queryObject = session.createQuery("delete from TestCaseIndex model where model.projectId = :projectId");
                queryObject.setParameter("projectId", projectId);
                SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());

                return queryObject.executeUpdate();
            }
        });
    }
}
