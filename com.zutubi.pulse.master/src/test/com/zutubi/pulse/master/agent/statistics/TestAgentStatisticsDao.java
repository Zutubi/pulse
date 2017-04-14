/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

    public AgentDailyStatistics findByAgentAndDaySafe(final long agentId, final long dayStamp)
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
