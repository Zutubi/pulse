package com.zutubi.pulse.master.xwork.actions.restore;

import com.zutubi.pulse.master.bootstrap.DefaultSetupManager;
import com.zutubi.pulse.master.bootstrap.SetupManager;

/**
 * Abort the restoration process, bypassing it and continuing on with
 * the pulse setup workflow.
 */
public class AbortRestoreAction extends RestoreActionSupport
{
    private SetupManager setupManager;

    public String execute() throws Exception
    {
        ((DefaultSetupManager)setupManager).doCancelRestorationRequest();
        return SUCCESS;
    }

    public void setSetupManager(SetupManager setupManager)
    {
        this.setupManager = setupManager;
    }
}
