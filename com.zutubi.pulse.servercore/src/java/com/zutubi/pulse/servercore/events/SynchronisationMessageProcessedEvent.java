package com.zutubi.pulse.servercore.events;

import com.zutubi.events.Event;
import com.zutubi.pulse.servercore.agent.SynchronisationMessageResult;

/**
 * Event raised when an asynchronous synchronisation message is processed by an
 * agent.
 */
public class SynchronisationMessageProcessedEvent extends Event
{
    private long agentId;
    private SynchronisationMessageResult result;

    public SynchronisationMessageProcessedEvent(Object source, long agentId, SynchronisationMessageResult result)
    {
        super(source);
        this.agentId = agentId;
        this.result = result;
    }

    public long getAgentId()
    {
        return agentId;
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

        SynchronisationMessageProcessedEvent that = (SynchronisationMessageProcessedEvent) o;

        if (agentId != that.agentId)
        {
            return false;
        }
        if (result != null ? !result.equals(that.result) : that.result != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result1 = (int) (agentId ^ (agentId >>> 32));
        result1 = 31 * result1 + (result != null ? result.hashCode() : 0);
        return result1;
    }

    @Override
    public String toString()
    {
        return "Synchronisation Message Processed Event: agent id: " + agentId + ", message id: " + result.getMessageId() + ", successful: " + result.isSuccessful();
    }
}
