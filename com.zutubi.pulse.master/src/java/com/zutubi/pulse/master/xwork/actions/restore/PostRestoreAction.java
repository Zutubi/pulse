package com.zutubi.pulse.master.xwork.actions.restore;

import com.zutubi.pulse.master.bootstrap.DefaultSetupManager;
import com.zutubi.pulse.master.bootstrap.SetupManager;

/**
 * The post restoration continue action, handles continuing setup once
 * the restoration process is complete.
 */
public class PostRestoreAction extends RestoreActionSupport
{
    private SetupManager setupManager;

    public String execute() throws Exception
    {
        ((DefaultSetupManager)setupManager).doCompleteRestoration();
        return SUCCESS;
    }

    public void setSetupManager(SetupManager setupManager)
    {
        this.setupManager = setupManager;
    }
}
