package com.zutubi.pulse.master.model.persistence.hibernate;

import com.google.common.base.Predicate;
import com.zutubi.pulse.master.model.AgentState;
import com.zutubi.pulse.master.model.AgentSynchronisationMessage;
import com.zutubi.pulse.master.model.persistence.AgentSynchronisationMessageDao;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.SessionFactoryUtils;

import java.util.List;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Lists.newArrayList;

/**
 * Hibernate-specific implementation of {@link AgentSynchronisationMessageDao}.
 */
@SuppressWarnings("unchecked")
public class HibernateAgentSynchronisationMessageDao extends HibernateEntityDao<AgentSynchronisationMessage> implements AgentSynchronisationMessageDao
{
    public Class<AgentSynchronisationMessage> persistentClass()
    {
        return AgentSynchronisationMessage.class;
    }

    public List<AgentSynchronisationMessage> findByAgentState(final AgentState agentState)
    {
        return getHibernateTemplate().execute(new HibernateCallback<List<AgentSynchronisationMessage>>()
        {
            public List<AgentSynchronisationMessage> doInHibernate(Session session) throws HibernateException
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
        return getHibernateTemplate().execute(new HibernateCallback<List<AgentSynchronisationMessage>>()
        {
            public List<AgentSynchronisationMessage> doInHibernate(Session session) throws HibernateException
            {
                Query queryObject = session.createQuery("from AgentSynchronisationMessage where statusName = :statusName");
                queryObject.setString("statusName", status.name());
                SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());
                return queryObject.list();
            }
        });
    }

    public List<AgentSynchronisationMessage> queryMessages(final AgentState agentState, final AgentSynchronisationMessage.Status status, final String taskType)
    {
        // CIB-3065: filtering by status and task type does not have much effect on large result
        // sizes as these will in practice be dominated by messages in the QUEUED state with the
        // CLEANUP_DIRECTORY type (at the moment).  Adding this criteria to the query has been
        // observed to make MySQL do a full table scan.  So just select all messages for the agent
        // and apply the filtering here.
        List<AgentSynchronisationMessage> messages = findByAgentState(agentState);
        return newArrayList(filter(messages, new Predicate<AgentSynchronisationMessage>()
        {
            public boolean apply(AgentSynchronisationMessage message)
            {
                return message.getStatus() == status && message.getMessage().getTypeName().equals(taskType);
            }
        }));
    }

    public int deleteByAgentState(final AgentState agentState)
    {
        return getHibernateTemplate().execute(new HibernateCallback<Integer>()
        {
            public Integer doInHibernate(Session session) throws HibernateException
            {
                Query queryObject = session.createQuery("delete from AgentSynchronisationMessage where agentState = :agentState");
                queryObject.setEntity("agentState", agentState);
                SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());
                return queryObject.executeUpdate();
            }
        });
    }
}