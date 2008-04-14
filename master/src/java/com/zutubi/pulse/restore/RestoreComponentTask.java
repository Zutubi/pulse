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

    public String getName()
    {
        String name = component.getName();
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    public ArchiveableComponent getComponent()
    {
        return component;
    }

    public boolean hasFailed()
    {
        return false;
    }

    public String[] getErrors()
    {
        return new String[0];
    }

    public boolean haltOnFailure()
    {
        return true;
    }

    public String getDescription()
    {
        return component.getDescription();
    }
}
