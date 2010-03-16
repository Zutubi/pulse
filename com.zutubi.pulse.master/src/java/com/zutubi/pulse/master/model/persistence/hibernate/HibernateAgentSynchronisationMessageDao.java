package com.zutubi.pulse.master.model.persistence.hibernate;

import com.zutubi.pulse.master.model.AgentState;
import com.zutubi.pulse.master.model.AgentSynchronisationMessage;
import com.zutubi.pulse.master.model.persistence.AgentSynchronisationMessageDao;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.SessionFactoryUtils;

import java.util.List;

/**
 * Hibernate-specific implementation of {@link com.zutubi.pulse.master.model.persistence.AgentSynchronisationMessageDao}.
 */
public class HibernateAgentSynchronisationMessageDao extends HibernateEntityDao<AgentSynchronisationMessage> implements AgentSynchronisationMessageDao
{
    public Class persistentClass()
    {
        return AgentSynchronisationMessage.class;
    }

    public List<AgentSynchronisationMessage> findByAgentState(final AgentState agentState)
    {
        return (List<AgentSynchronisationMessage>) getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException
            {
                Query queryObject = session.createQuery("from AgentSynchronisationMessage where agentState = :agentState order by id asc");
                queryObject.setEntity("agentState", agentState);
                SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());
                return queryObject.list();
            }
        });
    }

    public int deleteByAgentState(final AgentState agentState)
    {
        return (Integer) getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException
            {
                Query queryObject = session.createQuery("delete from AgentSynchronisationMessage where agentState = :agentState");
                queryObject.setEntity("agentState", agentState);
                SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());
                return queryObject.executeUpdate();
            }
        });
    }
}