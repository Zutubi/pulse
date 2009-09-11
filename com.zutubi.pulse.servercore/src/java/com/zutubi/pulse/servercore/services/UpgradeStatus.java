package com.zutubi.pulse.servercore.services;

/**
 * Data object that carries upgrade status information from agents back to the
 * master.
 */
public class UpgradeStatus
{
    // NOTE: These fields cannot change, as the old agent (running the upgrade)
    // code relies on thier names.  The handle in particular no longer holds a
    // handle (it is the host id as exposed via the API).
    private long handle;
    private UpgradeState state;
    private int progress;
    private String message;

    public UpgradeStatus(long hostId, UpgradeState state, int progress, String message)
    {
        this.handle = hostId;
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
