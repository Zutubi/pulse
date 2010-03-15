package com.zutubi.pulse.master.model;

import com.zutubi.pulse.core.model.Entity;

/**
 * Stores statistics for a single agent on a single day.  Records the time
 * spent in different status categories during that day.
 */
public class AgentDailyStatistics extends Entity
{
    private long agentId;
    private long dayStamp;
    private int recipeCount;
    private int disabledTime;
    private int offlineTime;
    private int synchronisingTime;
    private int idleTime;
    private int busyTime;

    public AgentDailyStatistics()
    {
    }

    public AgentDailyStatistics(long agentId, long dayStamp)
    {
        this.agentId = agentId;
        this.dayStamp = dayStamp;
    }

    /**
     * @return the id of the agent that these statistics were gathered for
     */
    public long getAgentId()
    {
        return agentId;
    }

    public void setAgentId(long agentId)
    {
        this.agentId = agentId;
    }

    /**
     * @return the number of recipes assigned to the agent on this day
     */
    public int getRecipeCount()
    {
        return recipeCount;
    }

    public void setRecipeCount(int recipeCount)
    {
        this.recipeCount = recipeCount;
    }

    /**
     * @return the time in millisecods since the epoch of the start of the day
     *         these statistics correspond to (midnight that day).
     */
    public long getDayStamp()
    {
        return dayStamp;
    }

    public void setDayStamp(long dayStamp)
    {
        this.dayStamp = dayStamp;
    }

    /**
     * @return the number of milliseconds that the agent spent disabled on this
     *         day
     */
    public int getDisabledTime()
    {
        return disabledTime;
    }

    public void setDisabledTime(int disabledTime)
    {
        this.disabledTime = disabledTime;
    }

    /**
     * @return the number of milliseconds that the agent spent offline on this
     *         day
     */
    public int getOfflineTime()
    {
        return offlineTime;
    }

    public void setOfflineTime(int offlineTime)
    {
        this.offlineTime = offlineTime;
    }

    /**
     * @return the number of milliseconds that the agent spent synchronising on
     *         this day
     */
    public int getSynchronisingTime()
    {
        return synchronisingTime;
    }

    public void setSynchronisingTime(int synchronisingTime)
    {
        this.synchronisingTime = synchronisingTime;
    }

    /**
     * @return the number of milliseconds that the agent spent idle on this day
     */
    public int getIdleTime()
    {
        return idleTime;
    }

    public void setIdleTime(int idleTime)
    {
        this.idleTime = idleTime;
    }

    /**
     * @return the number of milliseconds that the agent spent busy on this day
     */
    public int getBusyTime()
    {
        return busyTime;
    }

    public void setBusyTime(int busyTime)
    {
        this.busyTime = busyTime;
    }

    @Override
    public String toString()
    {
        return "AgentDailyStatistics{" +
                "agentId=" + agentId +
                ", dayStamp=" + dayStamp +
                ", recipeCount=" + recipeCount +
                ", disabledTime=" + disabledTime +
                ", offlineTime=" + offlineTime +
                ", idleTime=" + idleTime +
                ", busyTime=" + busyTime +
                '}';
    }
}
