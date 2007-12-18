package com.zutubi.pulse.web.restore;

import com.zutubi.pulse.bootstrap.SetupManager;

/**
 *
 *
 */
public class PostRestoreAction extends RestoreActionSupport
{
    private SetupManager setupManager;

    public String execute() throws Exception
    {
        setupManager.requestRestoreComplete(true);
        return SUCCESS;
    }

    public void setSetupManager(SetupManager setupManager)
    {
        this.setupManager = setupManager;
    }

}
