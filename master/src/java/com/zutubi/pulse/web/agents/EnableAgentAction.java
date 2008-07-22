package com.zutubi.pulse.web.agents;

import com.zutubi.pulse.events.AgentDisableRequestedEvent;
import com.zutubi.pulse.events.AgentEnableRequestedEvent;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.model.Slave;

/**
 */
public class EnableAgentAction extends AgentActionSupport
{
    private String enable;
    private EventManager eventManager;

    public void setEnable(String enable)
    {
        this.enable = enable;
    }

    public String execute() throws Exception
    {
        lookupSlave();
        if(slave != null)
        {
            if (Slave.EnableState.ENABLED.toString().equals(enable))
            {
                eventManager.publish(new AgentEnableRequestedEvent(this, getAgent()));
            }
            else
            {
                eventManager.publish(new AgentDisableRequestedEvent(this, getAgent()));
            }
        }

        return SUCCESS;
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }
}
