package com.zutubi.pulse.master.events;

import com.zutubi.pulse.master.agent.Agent;
import com.zutubi.pulse.master.agent.AgentStatus;

/**
 * Low level event raised when an agent's status changes.  Consider handling
 * higher-level events such as availability events if possible.
 *
 * @see AgentAvailableEvent
 * @see AgentOfflineEvent
 * @see AgentOnlineEvent
 * @see AgentUnavailableEvent
 */
public class AgentStatusChangeEvent extends AgentEvent
{
    private AgentStatus oldStatus;
    private AgentStatus newStatus;

    public AgentStatusChangeEvent(Object source, Agent agent, AgentStatus oldStatus, AgentStatus newStatus)
    {
        super(source, agent);
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
    }




    /**
     * Gives the status of the agent before the change.
     *
     * @return the previous status
     */
    public AgentStatus getOldStatus()
    {
        return oldStatus;
    }

    /**
     * Gives the status of the agent after the change.
     *
     * @return the new status
     */
    public AgentStatus getNewStatus()
    {
        return newStatus;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        AgentStatusChangeEvent that = (AgentStatusChangeEvent) o;
        if (!getAgent().equals(that.getAgent()))
        {
            return false;
        }
        if (newStatus != that.newStatus)
        {
            return false;
        }
        if (oldStatus != that.oldStatus)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = getAgent().hashCode();
        result = 31 * result + oldStatus.hashCode();
        result = 31 * result + newStatus.hashCode();
        return result;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder("Agent Status Change Event: ");
        if (getAgent() != null)
        {
            builder.append(getAgent().getConfig().getName()).append(": ");
        }
        builder.append(oldStatus);
        builder.append(" -> ");
        builder.append(newStatus);
        return builder.toString();
    }
}
