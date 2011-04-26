package com.zutubi.pulse.master.xwork.actions.agents;

import com.zutubi.pulse.master.agent.Agent;
import com.zutubi.pulse.master.xwork.actions.LookupErrorException;

/**
 * Action for the server and agent info tabs.
 */
public class ServerInfoAction extends AgentActionBase
{
    @Override
    public String execute() throws Exception
    {
        try
        {
            Agent agent = getAgent();
            if (agent != null && !agent.isOnline())
            {
                addActionError("Agent is not online.");
            }
        }
        catch (LookupErrorException e)
        {
            addActionError(e.getMessage());
        }

        return SUCCESS;
    }
}
