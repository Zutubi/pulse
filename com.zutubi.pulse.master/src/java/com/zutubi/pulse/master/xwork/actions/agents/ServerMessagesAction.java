package com.zutubi.pulse.master.xwork.actions.agents;

import com.zutubi.pulse.master.agent.Agent;
import com.zutubi.pulse.master.xwork.actions.LookupErrorException;
import com.zutubi.tove.security.AccessManager;

/**
 * Action for the server and agent messages tabs.
 */
public class ServerMessagesAction extends AgentActionBase
{
    private int startPage;

    public int getStartPage()
    {
        return startPage;
    }

    public void setStartPage(int startPage)
    {
        this.startPage = startPage;
    }

    @Override
    public String execute() throws Exception
    {
        accessManager.ensurePermission(AccessManager.ACTION_ADMINISTER, null);

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
