package com.zutubi.pulse.services;

/**
 */
public class UpgradeStatus
{
    private long slaveId;
    private UpgradeState state;
    private int progress;
    private String message;

    public UpgradeStatus(long slaveId, UpgradeState state, int progress, String message)
    {
        this.slaveId = slaveId;
        this.state = state;
        this.progress = progress;
        this.message = message;
    }

    public long getSlaveId()
    {
        return slaveId;
    }

    public UpgradeState getState()
    {
        return state;
    }

    public int getProgress()
    {
        return progress;
    }

    public String getMessage()
    {
        return message;
    }
}
