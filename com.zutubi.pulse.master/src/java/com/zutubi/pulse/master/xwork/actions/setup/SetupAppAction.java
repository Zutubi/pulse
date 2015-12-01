package com.zutubi.pulse.master.xwork.actions.setup;

import com.zutubi.pulse.master.bootstrap.SetupManager;
import com.zutubi.pulse.master.bootstrap.SetupState;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;

/**
 * Redirects to the correct place depending on whether we are still starting/setting up or not.
 */
public class SetupAppAction extends ActionSupport
{
    private SetupManager setupManager;

    public String execute() throws Exception
    {
        SetupState state = setupManager.getCurrentState();
        if (state == SetupState.STARTING)
        {
            return "redirect";
        }
        else
        {
            return "success";
        }
    }

    public void setSetupManager(SetupManager setupManager)
    {
        this.setupManager = setupManager;
    }
}
