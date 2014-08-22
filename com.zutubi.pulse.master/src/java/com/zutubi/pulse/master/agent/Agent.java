package com.zutubi.pulse.master.agent;

import com.zutubi.pulse.master.events.AgentPingEvent;
import com.zutubi.pulse.master.model.AgentState;
import com.zutubi.pulse.master.model.Comment;
import com.zutubi.pulse.master.tove.config.agent.AgentConfiguration;

import java.util.List;

/**
 * An abstraction over an agent, be it the local (master) agent or a slave.
 */
public interface Agent
{
    String getName();
    AgentStatus getStatus();
    Host getHost();

    long getId();

    void updateStatus(AgentPingEvent agentPingEvent, long timestamp);
    void updateStatus(AgentStatus status, long timestamp);
    void updateStatus(AgentStatus status, long timestamp, long buildId, long recipeId);

    void copyStatus(Agent agent);

    AgentConfiguration getConfig();
    AgentService getService();

    /**
     * @return timestamp, in milliseconds since the epoch, when this agent was last pinged; zero
     *         if it has not been pinged
     */
    long getLastPingTime();
    long getSecondsSincePing();
    /**
     * @return an error message indicating why the last ping failed, null when the ping succeeded
     */
    String getPingError();

    /**
     * @return timestamp, in milliseconds since the epoch, when this agent was last online; zero
     *         if it has not been online
     */
    long getLastOnlineTime();

    long getBuildId();
    long getRecipeId();

    boolean isOnline();
    boolean isEnabled();
    boolean isDisabling();
    boolean isDisabled();
    boolean isAvailable();

    AgentState.EnableState getEnableState();
    List<Comment> getComments();
    
    void setAgentState(AgentState agentState);
}
