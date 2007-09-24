package com.zutubi.pulse.web.agents;

import com.opensymphony.util.TextUtils;
import com.zutubi.prototype.actions.ActionManager;
import com.zutubi.pulse.prototype.config.agent.AgentConfiguration;
import com.zutubi.pulse.webwork.mapping.Urls;

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
        Urls urls = new Urls("");
        if(TextUtils.stringSet(tab))
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
