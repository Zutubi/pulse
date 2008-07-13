package com.zutubi.pulse.agent;

import com.zutubi.pulse.AgentService;
import com.zutubi.pulse.model.AgentState;
import com.zutubi.pulse.services.SlaveStatus;
import com.zutubi.pulse.services.UpgradeState;
import com.zutubi.pulse.tove.config.agent.AgentConfiguration;

import java.text.DateFormat;
import java.util.Date;

/**
 */
public class DefaultAgent implements Agent
{
    private AgentConfiguration agentConfig;
    /**
     * This cached copy of the state is read-only, as it is detached from the
     * session.  For this reason it should not be exposed beyond this object.
     */
    private AgentState agentState;
    private Status status;
    private long lastPingTime = 0;
    private AgentService agentService;
    private String pingError = null;
    /**
     * The upgrade state is only used when the slave enable state is UPGRADING.
     */
    private UpgradeState upgradeState = UpgradeState.INITIAL;
    private int upgradeProgress = -1;
    private String upgradeMessage = null;

    public DefaultAgent(AgentConfiguration agentConfig, AgentState agentState, AgentService agentService)
    {
        this.agentConfig = agentConfig;
        this.agentState = agentState;
        this.agentService = agentService;

        // Restore transient state based on persistent state
        switch(agentState.getEnableState())
        {
            case ENABLED:
                status = Status.OFFLINE;
                break;
            case DISABLED:
            case UPGRADING:
                status = Status.DISABLED;
                break;
            case FAILED_UPGRADE:
                status = Status.DISABLED;
                upgradeState = UpgradeState.FAILED;
                break;
        }
    }

    public AgentService getService()
    {
        return agentService;
    }

    public Status getStatus()
    {
        return status;
    }

    public String getLocation()
    {
        if(agentConfig.isRemote())
        {
            return agentConfig.getHost() + ":" + agentConfig.getPort();
        }
        else
        {
            return "[local]";
        }
    }

    public long getId()
    {
        return agentState.getId();
    }

    public void setStatus(Status status)
    {
        this.status = status;
    }

    public AgentConfiguration getConfig()
    {
        return agentConfig;
    }

    public long getLastPingTime()
    {
        return lastPingTime;
    }

    public boolean hasBeenPinged()
    {
        return lastPingTime != 0;
    }

    public String getPrettyPingTime()
    {
        if (hasBeenPinged())
        {
            return DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(new Date(lastPingTime));
        }
        else
        {
            return "<never>";
        }
    }

    public long getSecondsSincePing()
    {
        return (System.currentTimeMillis() - lastPingTime) / 1000;
    }

    public String getPingError()
    {
        return pingError;
    }

    public boolean isOnline()
    {
        return status.isOnline();
    }

    public boolean isEnabled()
    {
        return agentState.isEnabled();
    }

    public boolean isDisabled()
    {
        return agentState.isDisabled();
    }

    public boolean isUpgrading()
    {
        return agentState.getEnableState() == AgentState.EnableState.UPGRADING;
    }

    public boolean isFailedUpgrade()
    {
        return agentState.getEnableState() == AgentState.EnableState.FAILED_UPGRADE;
    }

    public boolean isAvailable()
    {
        return status == Status.IDLE;
    }

    public void updateStatus(SlaveStatus status)
    {
        lastPingTime = status.getPingTime();
        this.status = status.getStatus();
        pingError = status.getMessage();
    }

    public void upgradeStatus(UpgradeState state, int progress, String message)
    {
        upgradeState = state;
        upgradeProgress = progress;
        upgradeMessage = message;
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

    public AgentState.EnableState getEnableState()
    {
        return agentState.getEnableState();
    }

    public void setAgentState(AgentState agentState)
    {
        this.agentState = agentState;
    }
}
