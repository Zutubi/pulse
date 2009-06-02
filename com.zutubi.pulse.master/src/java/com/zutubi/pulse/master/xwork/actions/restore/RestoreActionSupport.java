package com.zutubi.pulse.master.xwork.actions.restore;

import com.zutubi.pulse.master.restore.RestoreManager;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;

/**
 * Base action for the pulse restoration process that can occur during
 * startup. 
 */
public class RestoreActionSupport extends ActionSupport
{
    protected RestoreManager restoreManager;

    public void setRestoreManager(RestoreManager restoreManager)
    {
        this.restoreManager = restoreManager;
    }
}
