package com.zutubi.pulse.master.xwork.actions.upgrade;

import com.zutubi.pulse.master.bootstrap.SetupManager;
import com.zutubi.pulse.master.security.AcegiUtils;

/**
 * <class-comment/>
 */
public class PostUpgradeAction extends UpgradeActionSupport
{
    private SetupManager setupManager;

    public String execute() throws Exception
    {
        AcegiUtils.runAsSystem(new Runnable()
        {
            public void run()
            {
                setupManager.requestUpgradeComplete(true);
            }
        });

        return SUCCESS;
    }

    public void setSetupManager(SetupManager setupManager)
    {
        this.setupManager = setupManager;
    }
}
