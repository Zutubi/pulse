package com.zutubi.pulse.master.events;

import com.zutubi.pulse.master.agent.Agent;
import com.zutubi.pulse.servercore.agent.SynchronisationMessageResult;

/**
 * Raised when an agent completes processing of a synchronisation message.
 * Note that some messages are processed immediately as they are sent, so no
 * such event is raised for them.  This event is only used for messages that
 * are processed asynchronously with respect to them being sent to the agent.
 */
public class AgentSynchronisationMessageProcessedEvent extends AgentEvent
{
    private SynchronisationMessageResult result;
    
    public AgentSynchronisationMessageProcessedEvent(Object source, Agent agent, SynchronisationMessageResult result)
    {
        super(source, agent);
        this.result = result;
    }

    public SynchronisationMessageResult getResult()
    {
        return result;
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

        AgentSynchronisationMessageProcessedEvent that = (AgentSynchronisationMessageProcessedEvent) o;

        if (result != null ? !result.equals(that.result) : that.result != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        return result != null ? result.hashCode() : 0;
    }

    @Override
    public String toString()
    {
        return "Agent Synchronisation Message Processed Event: agent: " +  getAgent().getConfig().getName() + ", id: " + result.getMessageId() + ", success: " + result.isSuccessful();
    }
}
