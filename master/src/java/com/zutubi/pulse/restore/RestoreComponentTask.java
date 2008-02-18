package com.zutubi.pulse.restore;

import java.io.File;

/**
 *
 *
 */
public class RestoreComponentTask implements RestoreTask
{
    private ArchiveableComponent component;
    private File archiveBase;

    public RestoreComponentTask(ArchiveableComponent component, File archiveBase)
    {
        this.component = component;
        this.archiveBase = archiveBase;
    }

    public void execute() throws ArchiveException
    {
        component.restore(archiveBase);
    }
}
