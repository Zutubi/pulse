package com.zutubi.pulse.master.xwork.actions.setup;

import com.zutubi.pulse.master.bootstrap.SetupManager;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;

/**
 * <class-comment/>
 */
public class SetupActionSupport extends ActionSupport
{
    protected SetupManager setupManager;

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
