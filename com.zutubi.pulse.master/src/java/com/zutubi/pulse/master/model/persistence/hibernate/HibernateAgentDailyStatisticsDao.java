package com.zutubi.pulse.master.model.persistence.hibernate;

import com.zutubi.pulse.master.model.AgentDailyStatistics;
import com.zutubi.pulse.master.model.persistence.AgentDailyStatisticsDao;
import com.zutubi.util.logging.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.SessionFactoryUtils;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

/**
 * Hibernate-specific implementation of {@link com.zutubi.pulse.master.model.persistence.AgentDailyStatisticsDao}.
 */
public class HibernateAgentDailyStatisticsDao extends HibernateEntityDao<AgentDailyStatistics> implements AgentDailyStatisticsDao
{
    private static final Logger LOG = Logger.getLogger(HibernateAgentDailyStatisticsDao.class);
    
    public Class persistentClass()
    {
        return AgentDailyStatistics.class;
    }

    @SuppressWarnings({"unchecked"})
    public List<AgentDailyStatistics> findByAgent(final long agentId)
    {
        return (List<AgentDailyStatistics>) getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException
            {
                Query queryObject = session.createQuery("from AgentDailyStatistics where agentId = :agentId");
                queryObject.setLong("agentId", agentId);
                SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());
                return queryObject.list();
            }
        });
    }

    public AgentDailyStatistics safeFindByAgentAndDay(final long agentId, final long dayStamp)
    {
        @SuppressWarnings({"unchecked"})
        List<AgentDailyStatistics> results = (List<AgentDailyStatistics>) getHibernateTemplate().execute(new HibernateCallback()
        {
            public Object doInHibernate(Session session) throws HibernateException
            {
                Query queryObject = session.createQuery("from AgentDailyStatistics where agentId = :agentId and dayStamp = :dayStamp");
                queryObject.setLong("agentId", agentId);
                queryObject.setLong("dayStamp", dayStamp);
                SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());
                return queryObject.list();
            }
        });
        
        if (results.size() == 0)
        {
            return null;
        }
        else if (results.size() == 1)
        {
            return results.get(0);
        }
        else
        {
            LOG.warning("Expected unique result for agent id '" + agentId + "' and day '" + dayStamp + "', but got " + results.size() + " results.  Resetting statistics for this day.");
            for (AgentDailyStatistics stats: results)
            {
                delete(stats);
            }
            
            return null;
        }
    }

    public int deleteByDayStampBefore(final long dayStamp)
    {
        return (Integer) getHibernateTemplate().execute(new HibernateCallback()
        {
            public Integer doInHibernate(Session session) throws HibernateException, SQLException
            {
                Query queryObject = session.createQuery("delete from AgentDailyStatistics where dayStamp < :dayStamp");
                queryObject.setLong("dayStamp", dayStamp);
                SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());
                return queryObject.executeUpdate();
            }
        });
    }

    public int deleteByAgentNotIn(final Set<Long> agentIds)
    {
        return (Integer) getHibernateTemplate().execute(new HibernateCallback()
        {
            public Integer doInHibernate(Session session) throws HibernateException, SQLException
            {
                Query queryObject = session.createQuery("delete from AgentDailyStatistics where agentId not in (:agentIds)");
                queryObject.setParameterList("agentIds", agentIds);
                SessionFactoryUtils.applyTransactionTimeout(queryObject, getSessionFactory());
                return queryObject.executeUpdate();
            }
        });
    }
}
