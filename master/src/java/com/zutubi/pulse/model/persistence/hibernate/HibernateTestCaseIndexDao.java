package com.zutubi.pulse.model.persistence.hibernate;

import com.zutubi.pulse.core.model.StoredFileArtifact;
import com.zutubi.pulse.core.model.TestCaseIndex;
import com.zutubi.pulse.core.model.Changelist;
import com.zutubi.pulse.model.persistence.FileArtifactDao;
import com.zutubi.pulse.model.persistence.TestCaseIndexDao;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.hibernate.Session;
import org.hibernate.HibernateException;
import org.hibernate.Query;

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
}
