package com.zutubi.pulse.web.agents;

import com.zutubi.pulse.agent.Agent;
import com.zutubi.pulse.events.AgentDisableRequestedEvent;
import com.zutubi.pulse.events.AgentEnableRequestedEvent;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.model.Slave;

/**
 * <class comment/>
 */
public class EnableMasterAction extends AgentActionSupport
{
    private boolean enable;
    private EventManager eventManager;

    public void setEnable(boolean enable)
    {
        this.enable = enable;
    }


    public String execute() throws Exception
    {
        Agent masterAgent = getAgentManager().getAgent((Slave) null);
        if (enable)
        {
            eventManager.publish(new AgentEnableRequestedEvent(this, masterAgent));
        }
        else
        {
            eventManager.publish(new AgentDisableRequestedEvent(this, masterAgent));
        }
        return SUCCESS;
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }
}
