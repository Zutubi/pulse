package com.zutubi.pulse.master.agent;

import com.zutubi.pulse.master.model.HostState;
import com.zutubi.pulse.servercore.services.UpgradeState;

/**
 * Default implementation of {@link Host}.
 */
public class DefaultHost implements Host
{
    /**
     * This is a detached entity that should be treated as read-only and not
     * accessible to the outside world.
     */
    private HostState state;
    /**
     * The upgrade state is only used when the persistent upgrade state is UPGRADING.
     */
    private UpgradeState upgradeState = UpgradeState.INITIAL;
    private int upgradeProgress = -1;
    private String upgradeMessage = null;

    public DefaultHost(HostState state)
    {
        this.state = state;
    }

    public long getId()
    {
        return state.getId();
    }

    public boolean isRemote()
    {
        return state.isRemote();
    }

    public String getHostName()
    {
        return state.getHostName();
    }

    public int getPort()
    {
        return state.getPort();
    }

    public String getLocation()
    {
        return getLocation(isRemote(), getHostName(), getPort());
    }

    public static String getLocation(boolean remote, String hostName, int port)
    {
        if (remote)
        {
            return (hostName == null ? "" : hostName) + ":" + port;
        }
        else
        {
            return HostState.LOCATION_MASTER;
        }
    }

    public HostState.PersistentUpgradeState getPersistentUpgradeState()
    {
        return state.getUpgradeState();
    }

    public boolean isUpgrading()
    {
        return state.getUpgradeState() != HostState.PersistentUpgradeState.NONE;
    }

    public UpgradeState getUpgradeState()
    {
        return upgradeState;
    }

    public int getUpgradeProgress()
    {
        return upgradeProgress;
    }

    public String getUpgradeMessage()
    {
        return upgradeMessage;
    }

    synchronized void upgradeStatus(UpgradeState state, int progress, String message)
    {
        upgradeState = state;
        upgradeProgress = progress;
        upgradeMessage = message;
    }

    synchronized void setState(HostState hostState)
    {
        state = hostState;
    }
}
