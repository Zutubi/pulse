package com.zutubi.pulse.web.restore;

import com.zutubi.pulse.bootstrap.SetupManager;
import com.zutubi.pulse.bootstrap.DefaultSetupManager;

/**
 *
 *
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
