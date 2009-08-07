package com.zutubi.pulse.master.xwork.actions.agents;

import com.zutubi.pulse.master.tove.config.agent.AgentConfiguration;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.tove.actions.ActionManager;
import com.zutubi.util.StringUtils;

/**
 * Used to execute a named config action with/on an agent.
 */
public class AgentActionAction extends AgentActionBase
{
    private String action;
    private String tab;
    private ActionManager actionManager;

    public void setAction(String action)
    {
        this.action = action;
    }

    public void setTab(String tab)
    {
        this.tab = tab;
    }

    public String getRedirect()
    {
        Urls urls = Urls.getBaselessInstance();
        if(StringUtils.stringSet(tab))
        {
            return urls.agent(getAgent()) + tab + "/";
        }
        else
        {
            return urls.agents();
        }
    }

    public String execute() throws Exception
    {
        AgentConfiguration config = getRequiredAgent().getConfig();

        try
        {
            actionManager.execute(action, config, null);
            return SUCCESS;
        }
        catch (Exception e)
        {
            addActionError(e.getMessage());
            return ERROR;
        }
    }

    public void setActionManager(ActionManager actionManager)
    {
        this.actionManager = actionManager;
    }
}
