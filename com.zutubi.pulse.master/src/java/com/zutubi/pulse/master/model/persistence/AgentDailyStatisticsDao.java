package com.zutubi.pulse.master.model.persistence;

import com.zutubi.pulse.master.model.AgentDailyStatistics;

import java.util.List;
import java.util.Set;

/**
 * Provides access to agent daily statistics entities.
 */
public interface AgentDailyStatisticsDao extends EntityDao<AgentDailyStatistics>
{
    /**
     * Finds all statistics for the given agent.
     *
     * @param agentId id of the agent to get the statistics for
     * @return all statistics for the specified agent
     */
    List<AgentDailyStatistics> findByAgent(long agentId);

    /**
     * Finds the statistics for the given agent on the given day.  May reset
     * the statistics for the agent/day combination if multiple entries are
     * found to match.
     *
     * @param agentId  id of the agent to get the statistics for
     * @param dayStamp stamp of the day (millisecond time for midnight that
     *                 starts the day)
     * @return the statistics for the given agent on the given day, or null if
     *         there are no such statistics yet
     */
    AgentDailyStatistics findByAgentAndDaySafe(long agentId, long dayStamp);

    /**
     * Deletes all statistics for days before the given day stamp.
     *
     * @param dayStamp stamp of the earliest day to keep statistics for
     * @return the number of rows deleted
     */
    int deleteByDayStampBefore(long dayStamp);

    /**
     * Deletes all statistics for agents that are not in the given set.
     *
     * @param agentIds set of agents to keep statistics for
     * @return the number of rows deleted
     */
    int deleteByAgentNotIn(final Set<Long> agentIds);
}