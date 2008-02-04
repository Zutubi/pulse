package com.zutubi.pulse.web.restore;

import com.zutubi.pulse.web.ActionSupport;
import com.zutubi.pulse.restore.ArchiveManager;

/**
 *
 *
 */
public class RestoreActionSupport extends ActionSupport
{
    protected ArchiveManager archiveManager;

    public void setArchiveManager(ArchiveManager archiveManager)
    {
        this.archiveManager = archiveManager;
    }
}
