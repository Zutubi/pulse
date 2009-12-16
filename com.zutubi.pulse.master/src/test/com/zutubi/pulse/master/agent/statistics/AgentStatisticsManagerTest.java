package com.zutubi.pulse.master.agent.statistics;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.agent.Agent;
import com.zutubi.pulse.master.agent.AgentManager;
import com.zutubi.pulse.master.agent.AgentStatus;
import com.zutubi.pulse.master.events.AgentStatusChangeEvent;
import com.zutubi.pulse.master.model.AgentDailyStatistics;
import com.zutubi.pulse.master.scheduling.Scheduler;
import com.zutubi.util.Constants;
import com.zutubi.util.TestClock;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import static com.zutubi.pulse.master.agent.AgentStatus.*;
import static java.util.Arrays.asList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class AgentStatisticsManagerTest extends PulseTestCase
{
    private static final Agent KNOWN_AGENT_1 = newAgent(1);
    private static final Agent KNOWN_AGENT_2 = newAgent(2);
    private static final Agent UNKNOWN_AGENT = newAgent(100);

    private TestClock clock;
    private TestAgentStatisticsDao agentDailyStatisticsDao;
    private AgentStatisticsManager agentStatisticsManager;
    private Long[] midnights = new Long[5];

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        for (int i = 0; i < midnights.length; i++)
        {
            midnights[i] = calendar.getTimeInMillis();
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        clock = new TestClock(midnights[0] + Constants.HOUR * 4);

        List<Agent> knownAgents = asList(KNOWN_AGENT_1, KNOWN_AGENT_2);

        AgentManager agentManager = mock(AgentManager.class);
        doReturn(knownAgents).when(agentManager).getAllAgents();

        agentDailyStatisticsDao = new TestAgentStatisticsDao();

        agentStatisticsManager = new AgentStatisticsManager();
        agentStatisticsManager.setClock(clock);
        agentStatisticsManager.setAgentManager(agentManager);
        agentStatisticsManager.setAgentDailyStatisticsDao(agentDailyStatisticsDao);
        agentStatisticsManager.setScheduler(mock(Scheduler.class));
        agentStatisticsManager.init();
    }

    public void testInitialEvent()
    {
        stateChange(KNOWN_AGENT_1, INITIAL, IDLE);
        List<AgentDailyStatistics> stats = agentStatisticsManager.getStatisticsForAgent(KNOWN_AGENT_1.getId());
        assertEquals(0, stats.size());
    }

    public void testTwoEventsForAgent()
    {
        final int DURATION = 10;

        stateChange(KNOWN_AGENT_1, INITIAL, IDLE);
        clock.add(DURATION);
        stateChange(KNOWN_AGENT_1, IDLE, OFFLINE);

        AgentDailyStatistics expected = new AgentDailyStatistics(KNOWN_AGENT_1.getId(), midnights[0]);
        expected.setIdleTime(DURATION);
        assertEquals(asList(expected), agentStatisticsManager.getStatisticsForAgent(KNOWN_AGENT_1.getId()));
    }

    public void testThreeEventsForAgent()
    {
        final int DURATION = 10;

        stateChange(KNOWN_AGENT_1, INITIAL, IDLE);
        clock.add(DURATION);
        stateChange(KNOWN_AGENT_1, IDLE, OFFLINE);
        clock.add(DURATION);
        stateChange(KNOWN_AGENT_1, OFFLINE, IDLE);
        
        AgentDailyStatistics expected = new AgentDailyStatistics(KNOWN_AGENT_1.getId(), midnights[0]);
        expected.setIdleTime(DURATION);
        expected.setOfflineTime(DURATION);
        assertEquals(asList(expected), agentStatisticsManager.getStatisticsForAgent(KNOWN_AGENT_1.getId()));
    }

    public void testMultipleEventsForSameCategory()
    {
        final int DURATION_1 = 10;
        final int DURATION_2 = 21;
        final int DURATION_3 = 45;

        stateChange(KNOWN_AGENT_1, INITIAL, OFFLINE);
        clock.add(DURATION_1);
        stateChange(KNOWN_AGENT_1, OFFLINE, INVALID_MASTER);
        clock.add(DURATION_2);
        stateChange(KNOWN_AGENT_1, INVALID_MASTER, TOKEN_MISMATCH);
        clock.add(DURATION_3);
        stateChange(KNOWN_AGENT_1, TOKEN_MISMATCH, IDLE);
        
        AgentDailyStatistics expected = new AgentDailyStatistics(KNOWN_AGENT_1.getId(), midnights[0]);
        expected.setOfflineTime(DURATION_1 + DURATION_2 + DURATION_3);
        assertEquals(asList(expected), agentStatisticsManager.getStatisticsForAgent(KNOWN_AGENT_1.getId()));
    }

    public void testRolloverViaEvents()
    {
        final int DURATION_1 = 10;
        final int DURATION_2 = 21;
        final int DURATION_3 = 2111;

        stateChange(KNOWN_AGENT_1, INITIAL, OFFLINE);
        clock.add(DURATION_1);
        stateChange(KNOWN_AGENT_1, OFFLINE, DISABLED);

        int timeToMidnight = (int) (midnights[1] - clock.getCurrentTimeMillis());
        clock.add(timeToMidnight + DURATION_2);

        stateChange(KNOWN_AGENT_1, DISABLED, INITIAL);

        AgentDailyStatistics expectedDay1 = new AgentDailyStatistics(KNOWN_AGENT_1.getId(), midnights[0]);
        expectedDay1.setOfflineTime(DURATION_1);
        expectedDay1.setDisabledTime(timeToMidnight);
        AgentDailyStatistics expectedDay2 = new AgentDailyStatistics(KNOWN_AGENT_1.getId(), midnights[1]);
        expectedDay2.setDisabledTime(DURATION_2);
        List<AgentDailyStatistics> expected = asList(expectedDay1, expectedDay2);
        assertEquals(expected, agentStatisticsManager.getStatisticsForAgent(KNOWN_AGENT_1.getId()));
        
        clock.add(DURATION_3);
        stateChange(KNOWN_AGENT_1, INITIAL, OFFLINE);
        expectedDay2.setOfflineTime(DURATION_3);
        assertEquals(expected, agentStatisticsManager.getStatisticsForAgent(KNOWN_AGENT_1.getId()));
    }

    public void testRecipeTracking()
    {
        final int DURATION_1 = 2310;
        final int DURATION_2 = 241;
        final int DURATION_3 = 111;
        final int DURATION_4 = 45;
        final int DURATION_5 = 577;
        final int DURATION_6 = 76;

        stateChange(KNOWN_AGENT_1, INITIAL, IDLE);
        clock.add(DURATION_1);
        stateChange(KNOWN_AGENT_1, IDLE, RECIPE_ASSIGNED);
        clock.add(DURATION_2);
        stateChange(KNOWN_AGENT_1, RECIPE_ASSIGNED, BUILDING);
        clock.add(DURATION_3);
        stateChange(KNOWN_AGENT_1, BUILDING, POST_RECIPE);
        clock.add(DURATION_4);
        stateChange(KNOWN_AGENT_1, POST_RECIPE, AWAITING_PING);
        clock.add(DURATION_5);
        stateChange(KNOWN_AGENT_1, AWAITING_PING, IDLE);

        AgentDailyStatistics expected = new AgentDailyStatistics(KNOWN_AGENT_1.getId(), midnights[0]);
        expected.setIdleTime(DURATION_1);
        expected.setBusyTime(DURATION_2 + DURATION_3 + DURATION_4 + DURATION_5);
        expected.setRecipeCount(1);
        assertEquals(asList(expected), agentStatisticsManager.getStatisticsForAgent(KNOWN_AGENT_1.getId()));

        clock.add(DURATION_6);
        stateChange(KNOWN_AGENT_1, IDLE, RECIPE_ASSIGNED);

        expected.setIdleTime(DURATION_1 + DURATION_6);
        expected.setRecipeCount(2);
        assertEquals(asList(expected), agentStatisticsManager.getStatisticsForAgent(KNOWN_AGENT_1.getId()));
    }

    public void testUpdateNoData()
    {
        agentStatisticsManager.update();
        List<AgentDailyStatistics> stats = agentStatisticsManager.getStatisticsForAgent(KNOWN_AGENT_1.getId());
        assertEquals(0, stats.size());
    }

    public void testUpdateCollectsGarbage()
    {
        AgentDailyStatistics notGarbage = new AgentDailyStatistics(KNOWN_AGENT_1.getId(), midnights[0]);
        agentDailyStatisticsDao.save(notGarbage);
        agentDailyStatisticsDao.save(new AgentDailyStatistics(KNOWN_AGENT_1.getId(), 0));
        agentDailyStatisticsDao.save(new AgentDailyStatistics(UNKNOWN_AGENT.getId(), midnights[0]));
        
        agentStatisticsManager.update();
        List<AgentDailyStatistics> stats = agentDailyStatisticsDao.findAll();
        assertEquals(asList(notGarbage), stats);
    }

    public void testUpdateAccumulatesTimes()
    {
        final int DURATION_1 = 10;
        final int DURATION_2 = 21;

        stateChange(KNOWN_AGENT_1, INITIAL, IDLE);
        clock.add(DURATION_1);
        agentStatisticsManager.update();

        AgentDailyStatistics expected = new AgentDailyStatistics(KNOWN_AGENT_1.getId(), midnights[0]);
        expected.setIdleTime(DURATION_1);
        assertEquals(asList(expected), agentStatisticsManager.getStatisticsForAgent(KNOWN_AGENT_1.getId()));

        // Verify a later event only adds time since update.
        clock.add(DURATION_2);
        stateChange(KNOWN_AGENT_1, IDLE, OFFLINE);

        expected.setIdleTime(DURATION_1 + DURATION_2);
        assertEquals(asList(expected), agentStatisticsManager.getStatisticsForAgent(KNOWN_AGENT_1.getId()));
    }

    public void testMultipleUpdates()
    {
        final int DURATION_1 = 10;
        final int DURATION_2 = 21;

        stateChange(KNOWN_AGENT_1, INITIAL, IDLE);
        clock.add(DURATION_1);
        agentStatisticsManager.update();

        AgentDailyStatistics expected = new AgentDailyStatistics(KNOWN_AGENT_1.getId(), midnights[0]);
        expected.setIdleTime(DURATION_1);
        assertEquals(asList(expected), agentStatisticsManager.getStatisticsForAgent(KNOWN_AGENT_1.getId()));

        clock.add(DURATION_2);
        agentStatisticsManager.update();

        expected.setIdleTime(DURATION_1 + DURATION_2);
        assertEquals(asList(expected), agentStatisticsManager.getStatisticsForAgent(KNOWN_AGENT_1.getId()));
    }

    public void testRolloverViaUpdate()
    {
        final int DURATION_1 = 10;
        final int DURATION_2 = 21;
        final int DURATION_3 = 2111;

        stateChange(KNOWN_AGENT_1, INITIAL, OFFLINE);
        clock.add(DURATION_1);
        stateChange(KNOWN_AGENT_1, OFFLINE, DISABLED);

        int timeToMidnight = (int) (midnights[1] - clock.getCurrentTimeMillis());
        clock.add(timeToMidnight + DURATION_2);

        agentStatisticsManager.update();

        AgentDailyStatistics expectedDay1 = new AgentDailyStatistics(KNOWN_AGENT_1.getId(), midnights[0]);
        expectedDay1.setOfflineTime(DURATION_1);
        expectedDay1.setDisabledTime(timeToMidnight);
        AgentDailyStatistics expectedDay2 = new AgentDailyStatistics(KNOWN_AGENT_1.getId(), midnights[1]);
        expectedDay2.setDisabledTime(DURATION_2);
        List<AgentDailyStatistics> expected = asList(expectedDay1, expectedDay2);
        assertEquals(expected, agentStatisticsManager.getStatisticsForAgent(KNOWN_AGENT_1.getId()));
        
        clock.add(DURATION_3);
        stateChange(KNOWN_AGENT_1, DISABLED, INITIAL);
        expectedDay2.setDisabledTime(DURATION_2 + DURATION_3);
        assertEquals(expected, agentStatisticsManager.getStatisticsForAgent(KNOWN_AGENT_1.getId()));
    }

    public void testMultipleAgents()
    {
        final int DURATION_1 = 2544;
        final int DURATION_2 = 300;
        final int DURATION_3 = 897;
        final int DURATION_4 = 4;
        final int DURATION_5 = 785;

        stateChange(KNOWN_AGENT_1, INITIAL, OFFLINE);
        clock.add(DURATION_1);
        stateChange(KNOWN_AGENT_2, INITIAL, IDLE);
        clock.add(DURATION_2);
        stateChange(KNOWN_AGENT_1, OFFLINE, IDLE);
        clock.add(DURATION_3);
        agentStatisticsManager.update();
        clock.add(DURATION_4);
        stateChange(KNOWN_AGENT_2, IDLE, DISABLED);
        clock.add(DURATION_5);
        agentStatisticsManager.update();

        AgentDailyStatistics expected1 = new AgentDailyStatistics(KNOWN_AGENT_1.getId(), midnights[0]);
        expected1.setOfflineTime(DURATION_1 + DURATION_2);
        expected1.setIdleTime(DURATION_3 + DURATION_4 + DURATION_5);
        assertEquals(asList(expected1), agentStatisticsManager.getStatisticsForAgent(KNOWN_AGENT_1.getId()));

        AgentDailyStatistics expected2 = new AgentDailyStatistics(KNOWN_AGENT_2.getId(), midnights[0]);
        expected2.setIdleTime(DURATION_2 + DURATION_3 + DURATION_4);
        expected2.setDisabledTime(DURATION_5);
        assertEquals(asList(expected2), agentStatisticsManager.getStatisticsForAgent(KNOWN_AGENT_2.getId()));
    }

    public void testSeveralDays()
    {
        List<AgentDailyStatistics> expected = new LinkedList<AgentDailyStatistics>();
        long start = clock.getCurrentTimeMillis();
        stateChange(KNOWN_AGENT_1, INITIAL, OFFLINE);
        for (int i = 1; i < midnights.length; i++)
        {
            long dayLength = midnights[i] - midnights[i - 1];
            clock.add(dayLength);
            agentStatisticsManager.update();

            AgentDailyStatistics stats = new AgentDailyStatistics(KNOWN_AGENT_1.getId(), midnights[i - 1]);
            stats.setOfflineTime((int) (midnights[i] - start));
            expected.add(stats);
            start = midnights[i];
        }

        AgentDailyStatistics stats = new AgentDailyStatistics(KNOWN_AGENT_1.getId(), start);
        stats.setOfflineTime((int) (clock.getCurrentTimeMillis() - start));
        expected.add(stats);

        assertEquals(expected, agentStatisticsManager.getStatisticsForAgent(KNOWN_AGENT_1.getId()));
    }

    private void assertEquals(List<AgentDailyStatistics> expected, List<AgentDailyStatistics> got)
    {
        assertEquals(expected.size(), got.size());
        for (int i = 0; i < expected.size(); i++)
        {
            assertEquals(expected.get(i), got.get(i));
        }
    }

    private void assertEquals(AgentDailyStatistics expected, AgentDailyStatistics got)
    {
        assertEquals(expected.getAgentId(), got.getAgentId());
        assertEquals(expected.getDayStamp(), got.getDayStamp());
        assertEquals(expected.getDisabledTime(), got.getDisabledTime());
        assertEquals(expected.getOfflineTime(), got.getOfflineTime());
        assertEquals(expected.getIdleTime(), got.getIdleTime());
        assertEquals(expected.getBusyTime(), got.getBusyTime());
        assertEquals(expected.getRecipeCount(), got.getRecipeCount());
    }

    private void stateChange(Agent agent, AgentStatus oldStatus, AgentStatus newStatus)
    {
        agentStatisticsManager.handleEvent(new AgentStatusChangeEvent(this, agent, oldStatus, newStatus));
    }

    private static Agent newAgent(long id)
    {
        Agent agent = mock(Agent.class);
        doReturn(id).when(agent).getId();
        return agent;
    }
}
