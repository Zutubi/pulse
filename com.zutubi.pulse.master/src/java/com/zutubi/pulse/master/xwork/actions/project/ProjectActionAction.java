package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.tove.actions.ActionManager;
import com.zutubi.util.StringUtils;

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
        Urls urls = Urls.getBaselessInstance();
        if(StringUtils.stringSet(tab))
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
        ProjectConfiguration config = getRequiredProject().getConfig();

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
