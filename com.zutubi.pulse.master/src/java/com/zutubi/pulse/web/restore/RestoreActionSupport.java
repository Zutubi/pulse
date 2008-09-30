package com.zutubi.pulse.web.restore;

import com.zutubi.pulse.web.ActionSupport;
import com.zutubi.pulse.restore.RestoreManager;

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
