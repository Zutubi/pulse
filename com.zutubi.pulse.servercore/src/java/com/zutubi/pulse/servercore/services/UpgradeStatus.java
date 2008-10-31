package com.zutubi.pulse.servercore.services;

/**
 */
public class UpgradeStatus
{
    private long handle;
    private UpgradeState state;
    private int progress;
    private String message;

    public UpgradeStatus(long handle, UpgradeState state, int progress, String message)
    {
        this.handle = handle;
        this.state = state;
        this.progress = progress;
        this.message = message;
    }

    public long getHandle()
    {
        return handle;
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
