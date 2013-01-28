package com.zutubi.pulse.master.agent.statistics;

import com.google.common.base.Predicate;
import com.zutubi.pulse.master.model.AgentDailyStatistics;
import com.zutubi.pulse.master.model.persistence.AgentDailyStatisticsDao;
import com.zutubi.pulse.master.model.persistence.InMemoryEntityDao;

import java.util.List;
import java.util.Set;

public class TestAgentStatisticsDao extends InMemoryEntityDao<AgentDailyStatistics> implements AgentDailyStatisticsDao
{
    public List<AgentDailyStatistics> findByAgent(final long agentId)
    {
        return findByPredicate(new Predicate<AgentDailyStatistics>()
        {
            public boolean apply(AgentDailyStatistics agentDailyStatistics)
            {
                return agentDailyStatistics.getAgentId() == agentId;
            }
        });
    }

    public AgentDailyStatistics safeFindByAgentAndDay(final long agentId, final long dayStamp)
    {
        return findUniqueByPredicate(new Predicate<AgentDailyStatistics>()
        {
            public boolean apply(AgentDailyStatistics agentDailyStatistics)
            {
                return agentDailyStatistics.getAgentId() == agentId && agentDailyStatistics.getDayStamp() == dayStamp;
            }
        });
    }

    public int deleteByDayStampBefore(final long dayStamp)
    {
        return deleteByPredicate(new Predicate<AgentDailyStatistics>()
        {
            public boolean apply(AgentDailyStatistics agentDailyStatistics)
            {
                return agentDailyStatistics.getDayStamp() < dayStamp;
            }
        });
    }

    public int deleteByAgentNotIn(final Set<Long> agentIds)
    {
        return deleteByPredicate(new Predicate<AgentDailyStatistics>()
        {
            public boolean apply(AgentDailyStatistics agentDailyStatistics)
            {
                return !agentIds.contains(agentDailyStatistics.getAgentId());
            }
        });
    }
}
