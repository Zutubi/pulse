package com.zutubi.pulse.web.restore;

import com.zutubi.pulse.bootstrap.SetupManager;

/**
 *
 *
 */
public class SkipRestoreAction extends RestoreActionSupport
{
    private SetupManager setupManager;

    public String execute() throws Exception
    {
        setupManager.requestRestoreComplete(false);
        return SUCCESS;
    }

    public void setSetupManager(SetupManager setupManager)
    {
        this.setupManager = setupManager;
    }

}
