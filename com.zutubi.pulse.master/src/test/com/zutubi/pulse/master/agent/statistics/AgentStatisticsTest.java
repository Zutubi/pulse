package com.zutubi.pulse.master.agent.statistics;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.model.AgentDailyStatistics;

import java.util.LinkedList;

import static java.util.Arrays.asList;

public class AgentStatisticsTest extends PulseTestCase
{
    public void testEmpty()
    {
        AgentStatistics stats = new AgentStatistics(new LinkedList<AgentDailyStatistics>());
        assertTrue(stats.isEmpty());
        assertEquals(0L, stats.getFirstDayStamp());
        assertEquals(0, stats.getTotalRecipes());
        assertEquals(0.0d, stats.getRecipesPerDay());
        assertEquals(0.0d, stats.getBusyTimePerRecipe());
        assertEquals(0L, stats.getTotalDisabledTime());
        assertEquals(0.0d, stats.getPercentDisabledTime());
        assertEquals(0L, stats.getTotalOfflineTime());
        assertEquals(0.0d, stats.getPercentOfflineTime());
        assertEquals(0L, stats.getTotalSynchronisingTime());
        assertEquals(0.0d, stats.getPercentSynchronisingTime());
        assertEquals(0L, stats.getTotalIdleTime());
        assertEquals(0.0d, stats.getPercentIdleTime());
        assertEquals(0L, stats.getTotalBusyTime());
        assertEquals(0.0d, stats.getPercentBusyTime());
    }

    public void testNonEmpty()
    {
        AgentDailyStatistics day1 = new AgentDailyStatistics(1, 2);
        day1.setOfflineTime(20);
        AgentDailyStatistics day2 = new AgentDailyStatistics(1, 1);
        day2.setOfflineTime(20);

        AgentStatistics stats = new AgentStatistics(asList(day1, day2));
        assertFalse(stats.isEmpty());
        assertEquals(1L, stats.getFirstDayStamp());
    }

    public void testDisabledTime()
    {
        AgentDailyStatistics day1 = new AgentDailyStatistics(1, 2);
        day1.setDisabledTime(10);
        day1.setOfflineTime(20);
        AgentDailyStatistics day2 = new AgentDailyStatistics(1, 2);
        day2.setDisabledTime(40);
        day2.setOfflineTime(30);
        
        AgentStatistics stats = new AgentStatistics(asList(day1, day2));
        assertEquals(50, stats.getTotalDisabledTime());
        assertEquals(50.0d, stats.getPercentDisabledTime());
    }

    public void testOfflineTime()
    {
        AgentDailyStatistics day1 = new AgentDailyStatistics(1, 2);
        day1.setOfflineTime(15);
        day1.setDisabledTime(20);
        AgentDailyStatistics day2 = new AgentDailyStatistics(1, 2);
        day2.setOfflineTime(40);
        day2.setDisabledTime(425);

        AgentStatistics stats = new AgentStatistics(asList(day1, day2));
        assertEquals(55, stats.getTotalOfflineTime());
        assertEquals(11.0d, stats.getPercentOfflineTime());
    }

    public void testSynchronisingTime()
    {
        AgentDailyStatistics day1 = new AgentDailyStatistics(1, 2);
        day1.setOfflineTime(20);
        day1.setSynchronisingTime(5);
        AgentDailyStatistics day2 = new AgentDailyStatistics(1, 2);
        day2.setOfflineTime(70);
        day2.setSynchronisingTime(5);

        AgentStatistics stats = new AgentStatistics(asList(day1, day2));
        assertEquals(10, stats.getTotalSynchronisingTime());
        assertEquals(10.0d, stats.getPercentSynchronisingTime());
    }

    public void testIdleTime()
    {
        AgentDailyStatistics day1 = new AgentDailyStatistics(1, 2);
        day1.setIdleTime(4);
        day1.setDisabledTime(20);
        AgentDailyStatistics day2 = new AgentDailyStatistics(1, 2);
        day2.setIdleTime(2);
        day2.setDisabledTime(24);

        AgentStatistics stats = new AgentStatistics(asList(day1, day2));
        assertEquals(6, stats.getTotalIdleTime());
        assertEquals(12.0d, stats.getPercentIdleTime());
    }

    public void testBusyTime()
    {
        AgentDailyStatistics day1 = new AgentDailyStatistics(1, 2);
        day1.setBusyTime(36);
        day1.setDisabledTime(20);
        AgentDailyStatistics day2 = new AgentDailyStatistics(1, 2);
        day2.setDisabledTime(144);

        AgentStatistics stats = new AgentStatistics(asList(day1, day2));
        assertEquals(36, stats.getTotalBusyTime());
        assertEquals(18.0d, stats.getPercentBusyTime());
    }

    public void testRecipeStats()
    {
        AgentDailyStatistics day1 = new AgentDailyStatistics(1, 2);
        day1.setRecipeCount(2);
        day1.setBusyTime(36);
        day1.setDisabledTime(20);
        AgentDailyStatistics day2 = new AgentDailyStatistics(1, 2);
        day2.setRecipeCount(6);
        day2.setDisabledTime(144);
        day2.setBusyTime(44);

        AgentStatistics stats = new AgentStatistics(asList(day1, day2));
        assertEquals(8, stats.getTotalRecipes());
        assertEquals(10.0, stats.getBusyTimePerRecipe());
    }
}
