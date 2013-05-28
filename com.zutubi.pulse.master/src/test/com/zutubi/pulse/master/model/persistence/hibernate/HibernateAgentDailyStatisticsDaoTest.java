package com.zutubi.pulse.master.model.persistence.hibernate;

import com.zutubi.pulse.master.model.AgentDailyStatistics;
import com.zutubi.pulse.master.model.persistence.AgentDailyStatisticsDao;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

public class HibernateAgentDailyStatisticsDaoTest extends MasterPersistenceTestCase
{
    private AgentDailyStatisticsDao agentDailyStatisticsDao;

    public void setUp() throws Exception
    {
        super.setUp();
        agentDailyStatisticsDao = (AgentDailyStatisticsDao) context.getBean("agentDailyStatisticsDao");
    }

    public void testSaveAndLoad()
    {
        AgentDailyStatistics stats = new AgentDailyStatistics(1, 2);
        stats.setRecipeCount(10);
        stats.setDisabledTime(3);
        stats.setOfflineTime(4);
        stats.setIdleTime(5);
        stats.setBusyTime(6);

        agentDailyStatisticsDao.save(stats);
        commitAndRefreshTransaction();

        AgentDailyStatistics anotherStats = agentDailyStatisticsDao.findById(stats.getId());

        assertNotSame(stats, anotherStats);
        assertPropertyEquals(stats, anotherStats);
    }

    public void testFindByAgent()
    {
        final long AGENT_ID = 22;
        final long OTHER_AGENT_ID = 23;

        AgentDailyStatistics agent1 = new AgentDailyStatistics(AGENT_ID, 0);
        AgentDailyStatistics otherAgent1 = new AgentDailyStatistics(OTHER_AGENT_ID, 0);
        AgentDailyStatistics agent2 = new AgentDailyStatistics(AGENT_ID, 0);
        AgentDailyStatistics otherAgent2 = new AgentDailyStatistics(OTHER_AGENT_ID, 0);

        agentDailyStatisticsDao.save(agent1);
        agentDailyStatisticsDao.save(otherAgent1);
        agentDailyStatisticsDao.save(agent2);
        agentDailyStatisticsDao.save(otherAgent2);

        List<AgentDailyStatistics> found = agentDailyStatisticsDao.findByAgent(AGENT_ID);
        assertEquals(2, found.size());
        assertThat(found, hasItem(agent1));
        assertThat(found, hasItem(agent2));
    }
    
    public void testSafeFindByAgentIdAndDay()
    {
        final long AGENT_ID = 22;
        final long OTHER_AGENT_ID = 23;
        final long DAY_STAMP = 12345;
        final long OTHER_DAY_STAMP = 21345;

        AgentDailyStatistics agentAndDay = new AgentDailyStatistics(AGENT_ID, DAY_STAMP);
        AgentDailyStatistics agentAndOtherDay = new AgentDailyStatistics(AGENT_ID, OTHER_DAY_STAMP);
        AgentDailyStatistics otherAgentAndDay = new AgentDailyStatistics(OTHER_AGENT_ID, DAY_STAMP);
        AgentDailyStatistics otherAgentAndOtherDay = new AgentDailyStatistics(OTHER_AGENT_ID, OTHER_DAY_STAMP);

        agentDailyStatisticsDao.save(agentAndDay);
        agentDailyStatisticsDao.save(agentAndOtherDay);
        agentDailyStatisticsDao.save(otherAgentAndDay);
        agentDailyStatisticsDao.save(otherAgentAndOtherDay);
        
        AgentDailyStatistics found = agentDailyStatisticsDao.findByAgentAndDaySafe(AGENT_ID, DAY_STAMP);
        assertEquals(agentAndDay, found);
    }

    public void testSafeFindByAgentIdAndDayMultipleHits()
    {
        final long AGENT_ID = 22;
        final long DAY_STAMP = 12345;

        AgentDailyStatistics agentAndDay = new AgentDailyStatistics(AGENT_ID, DAY_STAMP);
        AgentDailyStatistics sameAgentAndDay = new AgentDailyStatistics(AGENT_ID, DAY_STAMP);

        agentDailyStatisticsDao.save(agentAndDay);
        agentDailyStatisticsDao.save(sameAgentAndDay);
        
        AgentDailyStatistics found = agentDailyStatisticsDao.findByAgentAndDaySafe(AGENT_ID, DAY_STAMP);
        assertNull(found);
    }
    
    public void testDeleteByDayStampBefore()
    {
        final long DAY_STAMP = 21345;

        AgentDailyStatistics veryOld = new AgentDailyStatistics(1, DAY_STAMP - 1000000);
        AgentDailyStatistics slightlyOld = new AgentDailyStatistics(2, DAY_STAMP - 1);
        AgentDailyStatistics newish = new AgentDailyStatistics(3, DAY_STAMP);
        AgentDailyStatistics shinyAndNew = new AgentDailyStatistics(4, DAY_STAMP + 1000000);

        agentDailyStatisticsDao.save(veryOld);
        agentDailyStatisticsDao.save(slightlyOld);
        agentDailyStatisticsDao.save(newish);
        agentDailyStatisticsDao.save(shinyAndNew);
        
        assertEquals(2, agentDailyStatisticsDao.deleteByDayStampBefore(DAY_STAMP));
        List<AgentDailyStatistics> all = agentDailyStatisticsDao.findAll();
        assertEquals(2, all.size());
        assertThat(all, hasItem(newish));
        assertThat(all, hasItem(shinyAndNew));
    }

    public void testDeleteByAgentNotIn()
    {
        final long AGENT_1 = 1;
        final long AGENT_2 = 222;
        final long UNKNOWN_AGENT_1 = 33;
        final long UNKNOWN_AGENT_2 = 4444;

        AgentDailyStatistics unknownAgent1 = new AgentDailyStatistics(UNKNOWN_AGENT_1, 0);
        AgentDailyStatistics unknownAgent2 = new AgentDailyStatistics(UNKNOWN_AGENT_2, 0);
        AgentDailyStatistics knownAgent1 = new AgentDailyStatistics(AGENT_1, 0);
        AgentDailyStatistics knownAgent2 = new AgentDailyStatistics(AGENT_2, 0);

        agentDailyStatisticsDao.save(unknownAgent1);
        agentDailyStatisticsDao.save(unknownAgent2);
        agentDailyStatisticsDao.save(knownAgent1);
        agentDailyStatisticsDao.save(knownAgent2);
        
        assertEquals(2, agentDailyStatisticsDao.deleteByAgentNotIn(new HashSet<Long>(Arrays.asList(AGENT_1, AGENT_2, 123456L))));
        List<AgentDailyStatistics> all = agentDailyStatisticsDao.findAll();
        assertEquals(2, all.size());
        assertThat(all, hasItem(knownAgent1));
        assertThat(all, hasItem(knownAgent2));
    }
}