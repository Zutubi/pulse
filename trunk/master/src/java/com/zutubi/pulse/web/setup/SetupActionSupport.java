package com.zutubi.pulse.web.setup;

import com.zutubi.pulse.web.ActionSupport;
import com.zutubi.pulse.bootstrap.SetupManager;

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
