package com.zutubi.pulse.master.agent;

import com.zutubi.pulse.master.events.AgentPingEvent;
import com.zutubi.pulse.master.model.AgentState;
import com.zutubi.pulse.master.model.Comment;
import com.zutubi.pulse.master.tove.config.agent.AgentConfiguration;

import java.util.Collections;
import java.util.List;

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
    private AgentStatus status;
    private Host host;
    private long lastPingTime = 0;
    private long lastOnlineTime = 0;
    private long recipeId = -1;
    private AgentService agentService;
    private String pingError = null;

    public DefaultAgent(AgentConfiguration agentConfig, AgentState agentState, AgentService agentService, Host host)
    {
        this.agentConfig = agentConfig;
        this.agentState = agentState;
        this.agentService = agentService;
        this.host = host;

        // Restore transient state based on persistent state
        switch(host.getPersistentUpgradeState())
        {
            case NONE:
                switch(agentState.getEnableState())
                {
                    case ENABLED:
                        status = AgentStatus.INITIAL;
                        break;
                    case DISABLED:
                    case DISABLING:
                        status = AgentStatus.DISABLED;
                        break;
                }
                break;
            case FAILED_UPGRADE:
            case UPGRADING:
                status = AgentStatus.VERSION_MISMATCH;
                break;
        }
    }

    public AgentService getService()
    {
        return agentService;
    }

    public String getName()
    {
        return getConfig().getName();
    }

    public AgentStatus getStatus()
    {
        return status;
    }

    public Host getHost()
    {
        return host;
    }

    public long getId()
    {
        return agentState.getId();
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

    public long getSecondsSincePing()
    {
        return (System.currentTimeMillis() - lastPingTime) / 1000;
    }

    public long getLastOnlineTime()
    {
        return lastOnlineTime;
    }

    public boolean hasBeenOnline()
    {
        return lastOnlineTime != 0;
    }

    public long getSecondsSinceOnline()
    {
        return (System.currentTimeMillis() - lastOnlineTime) / 1000;
    }

    public long getRecipeId()
    {
        return recipeId;
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

    public boolean isDisabling()
    {
        return agentState.isDisabling();
    }

    public boolean isDisabled()
    {
        return agentState.isDisabled();
    }

    public boolean isAvailable()
    {
        return status.isAvailable();
    }

    public void updateStatus(AgentPingEvent agentPingEvent)
    {
        updateStatus(AgentStatus.valueOf(agentPingEvent.getPingStatus().toString()), agentPingEvent.getRecipeId(), agentPingEvent.getMessage());
    }

    public void updateStatus(AgentStatus status)
    {
        updateStatus(status, -1);
    }

    public void updateStatus(AgentStatus status, long recipeId)
    {
        updateStatus(status, recipeId, null);
    }

    public synchronized void updateStatus(AgentStatus status, long recipeId, String pingError)
    {
        lastPingTime = System.currentTimeMillis();
        if (status.isOnline())
        {
            lastOnlineTime = lastPingTime;
        }
        this.status = status;
        this.recipeId = recipeId;
        this.pingError = pingError;
    }

    public synchronized void copyStatus(Agent agent)
    {
        DefaultAgent existingAgent = (DefaultAgent) agent;
        status = existingAgent.status;
        lastPingTime = existingAgent.lastPingTime;
        lastOnlineTime = existingAgent.lastOnlineTime;
        recipeId = existingAgent.recipeId;
        pingError = existingAgent.pingError;
    }

    public AgentState.EnableState getEnableState()
    {
        return agentState.getEnableState();
    }

    public List<Comment> getComments()
    {
        return Collections.unmodifiableList(agentState.getComments());
    }

    public synchronized void setAgentState(AgentState agentState)
    {
        this.agentState = agentState;
    }

    @Override
    public String toString()
    {
        return getName() + "@" + getHost().getLocation() + ": " + status;
    }
}
