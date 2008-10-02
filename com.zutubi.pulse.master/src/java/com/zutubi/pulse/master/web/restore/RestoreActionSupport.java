package com.zutubi.pulse.master.web.restore;

import com.zutubi.pulse.master.restore.RestoreManager;
import com.zutubi.pulse.master.web.ActionSupport;

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
