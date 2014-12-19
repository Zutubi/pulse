package com.zutubi.pulse.master.xwork.actions.upgrade;

import com.zutubi.pulse.master.bootstrap.SetupManager;

/**
 * Action to continue server startup after an upgrade.
 */
public class PostUpgradeAction extends UpgradeActionSupport
{
    private SetupManager setupManager;

    public String execute() throws Exception
    {
        runOnce(new Runnable()
        {
            public void run()
            {
                setupManager.requestUpgradeComplete(true);
            }
        }, getClass().getName());

        return SUCCESS;
    }

    public void setSetupManager(SetupManager setupManager)
    {
        this.setupManager = setupManager;
    }
}
