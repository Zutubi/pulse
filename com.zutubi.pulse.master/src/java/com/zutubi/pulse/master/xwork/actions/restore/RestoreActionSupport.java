package com.zutubi.pulse.master.xwork.actions.restore;

import com.zutubi.pulse.master.restore.RestoreManager;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;

/**
 *
 *
 */
public class RestoreActionSupport extends ActionSupport
{
    protected RestoreManager restoreManager;

    public void setArchiveManager(RestoreManager restoreManager)
    {
        this.restoreManager = restoreManager;
    }
}
