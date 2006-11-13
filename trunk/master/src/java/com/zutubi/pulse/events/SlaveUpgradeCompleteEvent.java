package com.zutubi.pulse.events;

import com.zutubi.pulse.agent.SlaveAgent;

/**
 */
public class SlaveUpgradeCompleteEvent extends SlaveAgentEvent
{
    private boolean successful;

    public SlaveUpgradeCompleteEvent(Object source, SlaveAgent agent, boolean successful)
    {
        super(source, agent);
        this.successful = successful;
    }

    public boolean isSuccessful()
    {
        return successful;
    }
}
