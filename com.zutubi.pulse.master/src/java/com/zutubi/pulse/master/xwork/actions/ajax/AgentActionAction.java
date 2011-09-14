package com.zutubi.pulse.master.xwork.actions.ajax;

import com.zutubi.pulse.master.agent.Agent;
import com.zutubi.pulse.master.xwork.actions.agents.AgentActionBase;
import com.zutubi.tove.actions.ActionManager;
import com.zutubi.tove.config.api.ActionResult;
import com.zutubi.util.StringUtils;

/**
 * Simple ajax action for performing named actions on agents.
 */
public class AgentActionAction extends AgentActionBase
{
    private String actionName;

    private SimpleResult result;

    private ActionManager actionManager;

    public void setActionName(String actionName)
    {
        this.actionName = actionName;
    }

    public SimpleResult getResult()
    {
        return result;
    }

    @Override
    public String execute() throws Exception
    {
        if (!StringUtils.stringSet(actionName))
        {
            result = new SimpleResult(false, "Action name is required");
            return SUCCESS;
        }

        try
        {
            Agent agent = getRequiredAgent();
            ActionResult actionResult = actionManager.execute(actionName, agent.getConfig(), null);
            result = new SimpleResult(actionResult.getStatus() == ActionResult.Status.SUCCESS, actionResult.getMessage());
        }
        catch (Exception e)
        {
            result = new SimpleResult(false, e.getMessage());
        }

        return SUCCESS;
    }

    public void setActionManager(ActionManager actionManager)
    {
        this.actionManager = actionManager;
    }
}
