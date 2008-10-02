package com.zutubi.pulse.web.setup;

import com.zutubi.pulse.master.bootstrap.SetupManager;

/**
 * Redirects to the correct action based on the current setup state.
 */
public class SetupWorkflowAction extends SetupActionSupport
{
    private SetupManager setupManager;

    public String execute() throws Exception
    {
        return setupManager.getCurrentState().toString().toLowerCase();
    }

    public void setSetupManager(SetupManager setupManager)
    {
        this.setupManager = setupManager;
    }
}
