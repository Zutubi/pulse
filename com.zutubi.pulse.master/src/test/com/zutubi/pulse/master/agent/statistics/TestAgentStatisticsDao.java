package com.zutubi.pulse.master.agent.statistics;

import com.zutubi.pulse.master.model.AgentDailyStatistics;
import com.zutubi.pulse.master.model.persistence.AgentDailyStatisticsDao;
import com.zutubi.pulse.master.model.persistence.InMemoryEntityDao;
import com.zutubi.util.Predicate;

import java.util.List;
import java.util.Set;

public class TestAgentStatisticsDao extends InMemoryEntityDao<AgentDailyStatistics> implements AgentDailyStatisticsDao
{
    public List<AgentDailyStatistics> findByAgent(final long agentId)
    {
        return findByPredicate(new Predicate<AgentDailyStatistics>()
        {
            public boolean satisfied(AgentDailyStatistics agentDailyStatistics)
            {
                return agentDailyStatistics.getAgentId() == agentId;
            }
        });
    }

    public AgentDailyStatistics findByAgentAndDay(final long agentId, final long dayStamp)
    {
        return findUniqueByPredicate(new Predicate<AgentDailyStatistics>()
        {
            public boolean satisfied(AgentDailyStatistics agentDailyStatistics)
            {
                return agentDailyStatistics.getAgentId() == agentId && agentDailyStatistics.getDayStamp() == dayStamp;
            }
        });
    }

    public int deleteByDayStampBefore(final long dayStamp)
    {
        return deleteByPredicate(new Predicate<AgentDailyStatistics>()
        {
            public boolean satisfied(AgentDailyStatistics agentDailyStatistics)
            {
                return agentDailyStatistics.getDayStamp() < dayStamp;
            }
        });
    }

    public int deleteByAgentNotIn(final Set<Long> agentIds)
    {
        return deleteByPredicate(new Predicate<AgentDailyStatistics>()
        {
            public boolean satisfied(AgentDailyStatistics agentDailyStatistics)
            {
                return !agentIds.contains(agentDailyStatistics.getAgentId());
            }
        });
    }
}
