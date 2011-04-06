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
@SuppressWarnings("unchecked")
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

    public List<AgentSynchronisationMessage> findByStatus(final AgentSynchronisationMessage.Status status)
    {
        return (List<AgentSynchronisationMessage>) getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException
            {
                Query queryObject = session.createQuery("from AgentSynchronisationMessage where statusName = :statusName");
                queryObject.setString("statusName", status.name());
                SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());
                return queryObject.list();
            }
        });
    }

    public List<AgentSynchronisationMessage> queryMessages(final AgentState agentState, final AgentSynchronisationMessage.Status status, final String taskType, final String description)
    {
        return (List<AgentSynchronisationMessage>) getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException
            {
                Query queryObject = session.createQuery("from AgentSynchronisationMessage where agentState = :agentState and statusName = :statusName and message.typeName = :typeName and description = :description order by id asc");
                queryObject.setEntity("agentState", agentState);
                queryObject.setString("statusName", status.name());
                queryObject.setString("typeName", taskType);
                queryObject.setString("description", description);
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