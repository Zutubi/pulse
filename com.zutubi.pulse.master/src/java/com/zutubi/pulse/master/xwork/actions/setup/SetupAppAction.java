package com.zutubi.pulse.master.xwork.actions.setup;

import com.zutubi.pulse.master.bootstrap.WebManager;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;

/**
 * Redirects to the correct place depending on whether we are still starting/setting up or not.
 */
public class SetupAppAction extends ActionSupport
{
    private WebManager webManager;

    public String execute() throws Exception
    {
        if (webManager.isMainDeployed())
        {
            return "redirect";
        }
        else
        {
            return "success";
        }
    }

    public void setWebManager(WebManager webManager)
    {
        this.webManager = webManager;
    }
}
