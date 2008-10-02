package com.zutubi.pulse.web.restore;

import com.zutubi.pulse.master.restore.RestoreManager;
import com.zutubi.pulse.web.ActionSupport;

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
