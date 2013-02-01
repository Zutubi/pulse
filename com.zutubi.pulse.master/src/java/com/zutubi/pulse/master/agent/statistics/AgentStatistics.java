package com.zutubi.pulse.master.agent.statistics;

import com.google.common.base.Function;
import com.zutubi.pulse.master.model.AgentDailyStatistics;
import static com.zutubi.util.CollectionUtils.map;
import static com.zutubi.util.CollectionUtils.reduce;
import com.zutubi.util.math.LongAddition;

import java.util.List;

/**
 * Wrapper around statistics for a single agent over multiple days which
 * performs common calculations.
 */
public class AgentStatistics
{
    private static final double PERCENT = 100.0d;
    
    private List<AgentDailyStatistics> dailyStatistics;
    private int totalRecipes;
    private long totalDisabledTime;
    private long totalOfflineTime;
    private long totalSynchronisingTime;
    private long totalIdleTime;
    private long totalBusyTime;
    private long totalRecordedTime;

    /**
     * Create a statistics object that encompasses all of the given daily
     * statistics.
     *
     * @param dailyStatistics daily statistics to wrap with this object
     */
    public AgentStatistics(List<AgentDailyStatistics> dailyStatistics)
    {
        this.dailyStatistics = dailyStatistics;
        totalRecipes = (int) total(new ToRecipeCount());
        totalDisabledTime = total(new ToDisabledTime());
        totalOfflineTime = total(new ToOfflineTime());
        totalSynchronisingTime = total(new ToSynchronisingTime());
        totalIdleTime = total(new ToIdleTime());
        totalBusyTime = total(new ToBusyTime());
        totalRecordedTime = totalDisabledTime + totalOfflineTime + totalSynchronisingTime + totalIdleTime + totalBusyTime;
    }

    private long total(Function<AgentDailyStatistics, Long> function)
    {
        return reduce(map(dailyStatistics, function), 0L, new LongAddition());
    }

    private double percentage(long time)
    {
        if (totalRecordedTime == 0)
        {
            return 0;
        }
        else
        {
            return PERCENT * time / totalRecordedTime;
        }
    }

    /**
     * Indicates if there is any data in this object.
     *
     * @return true if there is no data collected in this object
     */
    public boolean isEmpty()
    {
        return dailyStatistics.size() == 0;
    }

    /**
     * Returns the day stamp for the earliest day recorded.  This stamp is from
     * the {@link com.zutubi.pulse.master.model.AgentDailyStatistics} instance,
     * see {@link com.zutubi.pulse.master.model.AgentDailyStatistics#getDayStamp()}.
     *
     * @return the day stamp for the earliest day in these statistics
     */
    public long getFirstDayStamp()
    {
        if (isEmpty())
        {
            return 0;
        }
        else
        {
            long min = Long.MAX_VALUE;
            for (AgentDailyStatistics day: dailyStatistics)
            {
                if (day.getDayStamp() < min)
                {
                    min = day.getDayStamp();
                }
            }

            return min;
        }
    }

    /**
     * Returns the total number of recipes run across all days.
     *
     * @return the total recipes run
     */
    public int getTotalRecipes()
    {
        return totalRecipes;
    }

    /**
     * Returns the average number of recipes run per day.
     *
     * @return average number of recipes per day
     */
    public double getRecipesPerDay()
    {
        if (isEmpty())
        {
            return 0;
        }
        else
        {
            return ((double) totalRecipes) / dailyStatistics.size();
        }
    }

    /**
     * Returns the average amount of busy time for each recipe.
     *
     * @return average busy time per recipe
     */
    public double getBusyTimePerRecipe()
    {
        if (totalRecipes > 0)
        {
            return ((double) totalBusyTime) / totalRecipes;
        }
        else
        {
            return 0;
        }
    }

    /**
     * Returns the total time the agent spent disabled over all days.
     *
     * @return total time spent disabled in milliseconds
     */
    public long getTotalDisabledTime()
    {
        return totalDisabledTime;
    }

    /**
     * Returns the proportion of time the agent spent disabled over all days.
     *
     * @return proportion of time spent disabled as a percentage (0-100).
     */
    public double getPercentDisabledTime()
    {
        return percentage(totalDisabledTime);
    }

    /**
     * Returns the total time the agent spent offline over all days.
     *
     * @return total time spent offline in milliseconds
     */
    public long getTotalOfflineTime()
    {
        return totalOfflineTime;
    }

    /**
     * Returns the proportion of time the agent spent offline over all days.
     *
     * @return proportion of time spent offline as a percentage (0-100).
     */
    public double getPercentOfflineTime()
    {
        return percentage(totalOfflineTime);
    }

    /**
     * Returns the total time the agent spent synchronising over all days.
     *
     * @return total time spent synchronising in milliseconds
     */
    public long getTotalSynchronisingTime()
    {
        return totalSynchronisingTime;
    }

    /**
     * Returns the proportion of time the agent spent synchronising over all
     * days.
     *
     * @return proportion of time spent synchronising as a percentage (0-100).
     */
    public double getPercentSynchronisingTime()
    {
        return percentage(totalSynchronisingTime);
    }

    /**
     * Returns the total time the agent spent idle over all days.
     *
     * @return total time spent idle in milliseconds
     */
    public long getTotalIdleTime()
    {
        return totalIdleTime;
    }

    /**
     * Returns the proportion of time the agent spent idle over all days.
     *
     * @return proportion of time spent idle as a percentage (0-100).
     */
    public double getPercentIdleTime()
    {
        return percentage(totalIdleTime);
    }

    /**
     * Returns the total time the agent spent busy over all days.
     *
     * @return total time spent busy in milliseconds
     */
    public long getTotalBusyTime()
    {
        return totalBusyTime;
    }

    /**
     * Returns the proportion of time the agent spent busy over all days.
     *
     * @return proportion of time spent busy as a percentage (0-100).
     */
    public double getPercentBusyTime()
    {
        return percentage(totalBusyTime);
    }

    private static class ToRecipeCount implements Function<AgentDailyStatistics, Long>
    {
        public Long apply(AgentDailyStatistics statistics)
        {
            return (long) statistics.getRecipeCount();
        }
    }

    private static class ToDisabledTime implements Function<AgentDailyStatistics, Long>
    {
        public Long apply(AgentDailyStatistics statistics)
        {
            return (long) statistics.getDisabledTime();
        }
    }

    private static class ToOfflineTime implements Function<AgentDailyStatistics, Long>
    {
        public Long apply(AgentDailyStatistics statistics)
        {
            return (long) statistics.getOfflineTime();
        }
    }

    private static class ToSynchronisingTime implements Function<AgentDailyStatistics, Long>
    {
        public Long apply(AgentDailyStatistics statistics)
        {
            return (long) statistics.getSynchronisingTime();
        }
    }

    private static class ToIdleTime implements Function<AgentDailyStatistics, Long>
    {
        public Long apply(AgentDailyStatistics statistics)
        {
            return (long) statistics.getIdleTime();
        }
    }

    private static class ToBusyTime implements Function<AgentDailyStatistics, Long>
    {
        public Long apply(AgentDailyStatistics statistics)
        {
            return (long) statistics.getBusyTime();
        }
    }
}
