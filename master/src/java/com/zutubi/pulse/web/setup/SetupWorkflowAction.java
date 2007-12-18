package com.zutubi.pulse.web.setup;

import com.zutubi.pulse.bootstrap.SetupManager;
import com.zutubi.pulse.bootstrap.SetupState;

/**
 * <class-comment/>
 */
public class SetupWorkflowAction extends SetupActionSupport
{
    private SetupManager setupManager;

    public String execute() throws Exception
    {
        SetupState s = setupManager.getCurrentState();

        if (s == SetupState.DATA)
        {
            return "data";
        }

        if (s == SetupState.LICENSE)
        {
            return "license";
        }

        if (s == SetupState.SETUP)
        {
            return "setup";
        }

        if (s == SetupState.UPGRADE)
        {
            return "upgrade";
        }

        if (s == SetupState.RESTORE)
        {
            return "restore";
        }

        if (s == SetupState.STARTING)
        {
            return "starting";
        }
        
        return SUCCESS;
    }


    /**
     * Required resource.
     *
     * @param setupManager
     */
    public void setSetupManager(SetupManager setupManager)
    {
        this.setupManager = setupManager;
    }
}
