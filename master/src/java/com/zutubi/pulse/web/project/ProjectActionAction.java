package com.zutubi.pulse.web.project;

import com.zutubi.util.TextUtils;
import com.zutubi.prototype.actions.ActionManager;
import com.zutubi.pulse.prototype.config.project.ProjectConfiguration;
import com.zutubi.pulse.webwork.mapping.Urls;

/**
 * Used to execute a named config action with/on a project.
 */
public class ProjectActionAction extends ProjectActionBase
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
            return urls.project(getProject()) + tab + "/";
        }
        else
        {
            return urls.projects();
        }
    }
    public String execute() throws Exception
    {
        ProjectConfiguration config =  getRequiredProject().getConfig();

        try
        {
            actionManager.execute(action, config, null);

            try
            {
                // Pause for dramatic effect
                Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
                // Empty
            }

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
